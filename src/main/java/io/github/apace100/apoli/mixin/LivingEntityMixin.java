package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.apace100.apoli.access.*;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.data.ApoliDamageTypes;
import io.github.apace100.apoli.networking.packet.s2c.SyncAttackerS2CPacket;
import io.github.apace100.apoli.power.type.*;
import io.github.apace100.apoli.util.InventoryUtil;
import io.github.apace100.apoli.util.SyncStatusEffectsUtil;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.Holder;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

@SuppressWarnings({"OptionalUsedAsFieldOrParameterType", "unused"})
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements ModifiableFoodEntity, MovingEntity, JumpingEntity {
    @Shadow
    protected abstract float getJumpPower();

    @Shadow
    public abstract float getSpeed();

    @Shadow
    private Optional<BlockPos> lastClimbablePos;

    @Shadow
    public abstract boolean isSuppressingSlidingDownLadder();

    @Shadow
    public abstract void setHealth(float health);

    public LivingEntityMixin(EntityType<?> type, Level world) {
        super(type, world);
    }

    @Inject(method = "onEffectAdded", at = @At("TAIL"))
    private void apoli$updateStatusEffectWhenApplied(MobEffectInstance effectInstance, Entity source, CallbackInfo ci) {
        SyncStatusEffectsUtil.sendStatusEffectUpdatePacket((LivingEntity) (Object) this, SyncStatusEffectsUtil.UpdateType.APPLY, effectInstance);
    }

    @Inject(method = "onEffectUpdated", at = @At("TAIL"))
    private void apoli$updateStatusEffectWhenUpgraded(MobEffectInstance effectInstance, boolean reapplyEffect, Entity source, CallbackInfo ci) {
        SyncStatusEffectsUtil.sendStatusEffectUpdatePacket((LivingEntity) (Object) this, SyncStatusEffectsUtil.UpdateType.UPGRADE, effectInstance);
    }

    @Inject(method = "onEffectsRemoved", at = @At("TAIL"))
    private void apoli$updateStatusEffectWhenRemoved(java.util.Collection<MobEffectInstance> effects, CallbackInfo ci) {
        for (MobEffectInstance effectInstance : effects) {
            SyncStatusEffectsUtil.sendStatusEffectUpdatePacket((LivingEntity) (Object) this, SyncStatusEffectsUtil.UpdateType.REMOVE, effectInstance);
        }
    }

    @Inject(method = "removeAllEffects", at = @At("RETURN"))
    private void apoli$updateStatusEffectWhenCleared(CallbackInfoReturnable<Boolean> cir) {
        SyncStatusEffectsUtil.sendStatusEffectUpdatePacket((LivingEntity) (Object) this, SyncStatusEffectsUtil.UpdateType.CLEAR, null);
    }

    @ModifyVariable(method = "addEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z", at = @At("HEAD"), argsOnly = true)
    private MobEffectInstance apoli$modifyStatusEffect(MobEffectInstance original) {

        Holder<MobEffect> effectType = original.getEffect();

        float amplifier = PowerHolderComponent.modify(this, ModifyStatusEffectAmplifierPowerType.class, original.getAmplifier(), p -> p.doesApply(effectType));
        float duration = PowerHolderComponent.modify(this, ModifyStatusEffectDurationPowerType.class, original.getDuration(), p -> p.doesApply(effectType));

        return new MobEffectInstance(
            effectType,
            Math.round(duration),
            Math.round(amplifier),
            original.isAmbient(),
            original.isVisible(),
            original.showIcon(),
            ((HiddenEffectStatus) original).apoli$getHiddenEffect()
        );

    }

    @Inject(method = "setLastHurtByMob", at = @At("TAIL"))
    private void apoli$syncAttacker(LivingEntity attacker, CallbackInfo ci) {

        if (this.level().isClientSide()) {
            return;
        }

        Optional<Integer> attackerId = Optional.ofNullable(this.getLastAttacker()).map(Entity::getId);
        SyncAttackerS2CPacket syncAttackerPacket = new SyncAttackerS2CPacket(this.getId(), attackerId);

        for (ServerPlayer player : PlayerLookup.tracking(this)) {
            ServerPlayNetworking.send(player, syncAttackerPacket);
        }

    }

    @ModifyReturnValue(method = "canStandOnFluid", at = @At("RETURN"))
    private boolean apoli$letEntitiesWalkOnFluid(boolean original, FluidState fluidState) {
        return original
            || PowerHolderComponent.hasPowerType(this, WalkOnFluidPowerType.class, p -> fluidState.is(p.getFluidTag()));
    }

    @ModifyVariable(method = "heal", at = @At("HEAD"), argsOnly = true)
    private float modifyHealingApplied(float originalValue) {
        return PowerHolderComponent.modify(this, ModifyHealingPowerType.class, originalValue);
    }

    @Unique
    private boolean apoli$hasModifiedDamage;

    @Unique
    private Optional<Boolean> apoli$shouldApplyArmor = Optional.empty();

    @Unique
    private Optional<Boolean> apoli$shouldDamageArmor = Optional.empty();

    @ModifyExpressionValue(method = "handleDamageEvent", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/damagesource/DamageSources;generic()Lnet/minecraft/world/damagesource/DamageSource;"))
    private DamageSource apoli$overrideDamageSourceOnSync(DamageSource original, DamageSource source) {
        return new net.minecraft.world.damagesource.DamageSource(this.level().registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.DAMAGE_TYPE).getOrThrow(ApoliDamageTypes.SYNC_DAMAGE_SOURCE));
    }

    @ModifyVariable(method = "hurtServer", at = @At("HEAD"), argsOnly = true)
    private float apoli$modifyDamageTaken(float original, net.minecraft.server.level.ServerLevel serverLevel, DamageSource source, float amount) {

        if (source.is(ApoliDamageTypes.SYNC_DAMAGE_SOURCE)) {
            return original;
        }

        LivingEntity thisAsLiving = (LivingEntity) (Object) this;
        float newValue = original;

        if (source.getEntity() != null && source.is(DamageTypeTags.IS_PROJECTILE)) {
            newValue = PowerHolderComponent.modify(source.getEntity(), ModifyProjectileDamagePowerType.class, original,
                p -> p.doesApply(source, original, thisAsLiving),
                p -> p.executeActions(thisAsLiving));
        } else if (source.getEntity() != null) {
            newValue = PowerHolderComponent.modify(source.getEntity(), ModifyDamageDealtPowerType.class, original,
                p -> p.doesApply(source, original, thisAsLiving),
                p -> p.executeActions(thisAsLiving));
        }

        float intermediateValue = newValue;
        newValue = PowerHolderComponent.modify(this, ModifyDamageTakenPowerType.class, intermediateValue,
            p -> p.doesApply(source, intermediateValue),
            p -> p.executeActions(source.getEntity()));

        apoli$hasModifiedDamage = newValue != original;
        List<ModifyDamageTakenPowerType> modifyDamageTakenPowers = PowerHolderComponent.getPowerTypes(this, ModifyDamageTakenPowerType.class)
            .stream()
            .filter(mdtp -> mdtp.doesApply(source, original))
            .toList();

        long wantArmor = modifyDamageTakenPowers
            .stream()
            .filter(mdtp -> mdtp.modifiesArmorApplicance() && mdtp.shouldApplyArmor())
            .count();
        long dontWantArmor = modifyDamageTakenPowers
            .stream()
            .filter(mdtp -> mdtp.modifiesArmorApplicance() && !mdtp.shouldApplyArmor())
            .count();
        apoli$shouldApplyArmor = wantArmor == dontWantArmor ? Optional.empty() : Optional.of(wantArmor > dontWantArmor);

        long wantDamage = modifyDamageTakenPowers
            .stream()
            .filter(mdtp -> mdtp.modifiesArmorDamaging() && mdtp.shouldDamageArmor())
            .count();
        long dontWantDamage = modifyDamageTakenPowers
            .stream()
            .filter(mdtp -> mdtp.modifiesArmorDamaging() && !mdtp.shouldDamageArmor())
            .count();
        apoli$shouldDamageArmor = wantDamage == dontWantDamage ? Optional.empty() : Optional.of(wantDamage > dontWantDamage);

        return newValue;

    }

    @ModifyExpressionValue(method = "getDamageAfterArmorAbsorb", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/damagesource/DamageSource;is(Lnet/minecraft/tags/TagKey;)Z"))
    private boolean apoli$allowApplyingOrDamagingArmor(boolean original, DamageSource source, float amount) {

        if (apoli$shouldApplyArmor.isEmpty() && (original && apoli$shouldDamageArmor.orElse(false))) {
            this.hurtArmor(source, amount);
        }

        return apoli$shouldApplyArmor
            .map(result -> !result)
            .orElse(original);

    }

    @WrapWithCondition(method = "getDamageAfterArmorAbsorb", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;hurtArmor(Lnet/minecraft/world/damagesource/DamageSource;F)V"))
    private boolean apoli$allowDamagingArmor(LivingEntity instance, DamageSource source, float amount) {
        return apoli$shouldDamageArmor.orElse(true);
    }

    @ModifyExpressionValue(method = "getDamageAfterArmorAbsorb", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/damagesource/CombatRules;getDamageAfterAbsorb(Lnet/minecraft/world/entity/LivingEntity;FLnet/minecraft/world/damagesource/DamageSource;FF)F"))
    private float apoli$allowApplyingArmor(float modified, DamageSource source, float original) {
        return apoli$shouldApplyArmor.orElse(true)
            ? modified
            : original;
    }

    @ModifyExpressionValue(method = "hurtServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isDeadOrDying()Z", ordinal = 0))
    private boolean apoli$preventHitIfDamageIsZero(boolean original, net.minecraft.server.level.ServerLevel serverLevel, DamageSource source, float amount) {
        return original || apoli$hasModifiedDamage && amount <= 0.0F;
    }

    @Inject(method = "hurtServer", at = @At("RETURN"), slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isSleeping()Z")))
    private void apoli$invokeHitActions(net.minecraft.server.level.ServerLevel serverLevel, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {

        if (!cir.getReturnValue()) {
            return;
        }

        Entity attacker = source.getEntity();

        PowerHolderComponent.withPowerTypes(this, ActionWhenHitPowerType.class, p -> p.doesApply(attacker, source, amount), p -> p.whenHit(attacker));
        PowerHolderComponent.withPowerTypes(attacker, ActionOnHitPowerType.class, p -> p.doesApply(this, source, amount), p -> p.onHit(this));

        PowerHolderComponent.withPowerTypes(this, ActionWhenDamageTakenPowerType.class, p -> p.doesApply(source, amount), ActionWhenDamageTakenPowerType::whenHit);
        PowerHolderComponent.withPowerTypes(this, AttackerActionWhenHitPowerType.class, p -> p.doesApply(source, amount), p -> p.whenHit(attacker));

        PowerHolderComponent.withPowerTypes(attacker, SelfActionOnHitPowerType.class, p -> p.doesApply(this, source, amount), SelfActionOnHitPowerType::onHit);
        PowerHolderComponent.withPowerTypes(attacker, TargetActionOnHitPowerType.class, p -> p.doesApply(this, source, amount), p -> p.onHit(this));

    }

    @Inject(method = "hurtServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;die(Lnet/minecraft/world/damagesource/DamageSource;)V"))
    private void invokeDeathAction(net.minecraft.server.level.ServerLevel serverLevel, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        PowerHolderComponent.withPowerTypes(this, ActionOnDeathPowerType.class, p -> p.doesApply(source.getEntity(), source, amount), p -> p.onDeath(source.getEntity()));
    }

    @Inject(method = "hurtServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;die(Lnet/minecraft/world/damagesource/DamageSource;)V"))
    private void invokeKillAction(net.minecraft.server.level.ServerLevel serverLevel, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        PowerHolderComponent.withPowerTypes(source.getEntity(), SelfActionOnKillPowerType.class, p -> p.doesApply(this, source, amount), SelfActionOnKillPowerType::executeAction);
    }

    @ModifyExpressionValue(method = "baseTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isWet()Z"))
    private boolean apoli$preventExtinguishingWhenPowerSwimming(boolean original) {

        if (this.isSwimming() && this.getFluidHeight(FluidTags.WATER) <= 0 && PowerHolderComponent.hasPowerType(this, SwimmingPowerType.class)) {
            return false;
        }

        else {
            return original;
        }

    }

    @Unique
    private boolean prevPowderSnowState = false;

    @Inject(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getTicksFrozen()I"))
    private void freezeEntityFromPower(CallbackInfo ci) {
        if(PowerHolderComponent.hasPowerType(this, FreezePowerType.class)) {
            this.prevPowderSnowState = this.isInPowderSnow;
            this.setIsInPowderSnow(true);
        }
    }

    @Inject(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;canFreeze()Z"))
    private void unfreezeEntityFromPower(CallbackInfo ci) {
        if(PowerHolderComponent.hasPowerType(this, FreezePowerType.class)) {
            this.setIsInPowderSnow(this.prevPowderSnowState);
        }
    }

    @Inject(method = "canFreeze", at = @At("RETURN"), cancellable = true)
    private void allowFreezingPower(CallbackInfoReturnable<Boolean> cir) {
        if(PowerHolderComponent.hasPowerType(this, FreezePowerType.class)) {
            cir.setReturnValue(true);
        }
    }

    @Unique
    private boolean apoli$applySprintJumpingEffects;

    @Override
    public boolean apoli$applySprintJumpEffects() {
        return apoli$applySprintJumpingEffects;
    }

    // SPRINT_JUMP
    @ModifyReturnValue(method = "getJumpPower()F", at = @At("RETURN"))
    private float apoli$modifyJumpVelocity(float original) {

        float modified = PowerHolderComponent.modify(this, ModifyJumpPowerType.class, original, p -> true, ModifyJumpPowerType::executeAction);
        this.apoli$applySprintJumpingEffects = modified > 0;

        return modified;

    }

    @ModifyExpressionValue(method = "jumpFromGround", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isSprinting()Z"))
    private boolean apoli$shouldApplySprintJumpEffects(boolean original) {
        return original && this.apoli$applySprintJumpEffects();
    }

    // HOTBLOODED
    @ModifyReturnValue(method = "canBeAffected", at = @At("RETURN"))
    private boolean apoli$effectImmunity(boolean original, MobEffectInstance effectInstance) {
        return original
            && !PowerHolderComponent.hasPowerType(this, EffectImmunityPowerType.class, p -> p.doesApply(effectInstance));
    }

    // CLIMBING
    @ModifyReturnValue(method = "onClimbable", at = @At("RETURN"))
    private boolean apoli$modifyClimbing(boolean original) {

        if (original) {
            return true;
        }

        List<ClimbingPowerType> climbingPowers = PowerHolderComponent.getPowerTypes(this, ClimbingPowerType.class);
        if (this.isSpectator() || climbingPowers.isEmpty()) {
            return false;
        }

        this.lastClimbablePos = Optional.of(this.blockPosition());
        return true;

    }

    @ModifyReturnValue(method = "isSuppressingSlidingDownLadder", at = @At("RETURN"))
    private boolean apoli$overrideClimbHold(boolean original) {

        List<ClimbingPowerType> climbingPowers = PowerHolderComponent.getPowerTypes(this, ClimbingPowerType.class);
        if (climbingPowers.isEmpty()) {
            return original;
        }

        return climbingPowers
            .stream()
            .anyMatch(ClimbingPowerType::canHold);

    }

    // SLOW_FALLING
    // TODO: MC 26.1 refactored travel() into travelInAir()/travelInFluid()/travelFallFlying().
    // The gravity variable 'd' (now 'movementY') moved from travel() to travelInAir().
    // Retargeted to travelInAir at the getEffectiveGravity() invoke, which is where
    // gravity is subtracted from movementY.
    @ModifyVariable(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getEffectiveGravity()D"), method = "travelInAir", ordinal = 0)
    public double modifyFallingVelocity(double original) {

        if (this.getDeltaMovement().y > 0D) {
            return original;
        }

        if (ModifyFallingPowerType.shouldNegateFallDamage(this)) {
            this.fallDistance = 0;
        }

        return PowerHolderComponent.modify(this, ModifyFallingPowerType.class, original);

    }

    @Inject(method = "getAttributes", at = @At("RETURN"))
    private void apoli$setAttributeContainerOwner(CallbackInfoReturnable<AttributeMap> cir) {

        if (cir.getReturnValue() instanceof OwnableAttributeContainer ownableAttributeContainer) {
            ownableAttributeContainer.apoli$setOwner(this);
        }

    }

    // TODO: MC 26.1 refactored travel() - the friction/slipperiness logic moved to travelInAir().
    // The onGround() call and blockFriction variable are now in travelInAir.
    @ModifyVariable(method = "travelInAir", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;onGround()Z", ordinal = 0))
    private float modifySlipperiness(float original) {
        return PowerHolderComponent.modify(this, ModifySlipperinessPowerType.class, original, p -> p.doesApply(this.level(), this.getBlockPosBelowThatAffectsMyMovement()));
    }

    @ModifyExpressionValue(method = "hurtServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isDeadOrDying()Z", ordinal = 1))
    private boolean apoli$preventDeath(boolean original, net.minecraft.server.level.ServerLevel serverLevel, DamageSource source, float amount) {

        if (original && PreventDeathPowerType.doesPrevent(this, source, amount)) {
            this.setHealth(1.0F);
            return false;
        }

        return original;

    }

    // TODO: MC 26.1 removed LivingEntity.eat() and applyEffectsFromMobEffect().
    // The food system has been reworked. ModifyFoodPowerType and EdibleItemPowerType
    // integration for non-player entities needs to be reimplemented for MC 26.1.

    @Shadow public abstract LivingEntity getLastAttacker();

    @Shadow protected abstract void hurtArmor(DamageSource source, float amount);

    @Shadow public abstract int getArmorValue();

    @Shadow public abstract AttributeMap getAttributes();

    @Shadow public abstract boolean onClimbable();

    @Shadow public abstract boolean isDeadOrDying();

    @Shadow public abstract double getAttributeValue(Holder<Attribute> attribute);

    @Shadow public abstract float getArmorCoverPercentage();

    @Shadow public abstract boolean hurtServer(net.minecraft.server.level.ServerLevel serverLevel, DamageSource source, float amount);

    @Shadow protected abstract void onEffectsRemoved(java.util.Collection<MobEffectInstance> effects);

    @Shadow public float xxa;

    @Shadow public float zza;

    @Shadow public abstract double getAttributeBaseValue(Holder<Attribute> attribute);

    @Inject(method = "getFlyingSpeed", at = @At("RETURN"), cancellable = true)
    private void modifyFlySpeed(CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue(PowerHolderComponent.modify(this, ModifyAirSpeedPowerType.class, cir.getReturnValue()));
    }

    @WrapOperation(method = "getVisibilityPercent", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isInvisible()Z"))
    private boolean apoli$specificallyInvisibleTo(LivingEntity livingEntity, Operation<Boolean> original, @Nullable Entity viewer) {

        List<InvisibilityPowerType> invisibilityPowers = PowerHolderComponent.getPowerTypes(livingEntity, InvisibilityPowerType.class, true);
        if (viewer == null || invisibilityPowers.isEmpty()) {
            return original.call(livingEntity);
        }

        return invisibilityPowers
            .stream()
            .anyMatch(p -> p.isActive() && p.doesApply(viewer));

    }

    @ModifyExpressionValue(method = "jumpFromGround", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isSprinting()Z"))
    private boolean apoli$cancelOutJumpVelocityIfNotMovingWithSprintPower(boolean original) {
        // The movement check is here so this doesn't happen if the player is moving at a sprinting amount.
        if (PowerHolderComponent.hasPowerType(this, SprintingPowerType.class) && this.apoli$getHorizontalMovementValue() < this.getSpeed()) {
            return false;
        }
        return original;
    }

    @Unique
    private List<ModifyFoodPowerType> apoli$currentModifyFoodPowers = new LinkedList<>();

    @Unique
    private ItemStack apoli$originalFoodStack;

    @Unique
    private EdibleItemPowerType apoli$edibleItemPower;

    @Override
    public List<ModifyFoodPowerType> apoli$getCurrentModifyFoodPowers() {
        return apoli$currentModifyFoodPowers;
    }

    @Override
    public void apoli$setCurrentModifyFoodPowers(List<ModifyFoodPowerType> powers) {
        apoli$currentModifyFoodPowers = powers;
    }

    @Override
    public ItemStack apoli$getOriginalFoodStack() {
        return apoli$originalFoodStack;
    }

    @Override
    public void apoli$setOriginalFoodStack(ItemStack original) {
        apoli$originalFoodStack = original;
    }

    @Override
    public EdibleItemPowerType apoli$getEdibleItemPower() {
        return apoli$edibleItemPower;
    }

    @Override
    public void apoli$setEdibleItemPower(EdibleItemPowerType power) {
        this.apoli$edibleItemPower = power;
    }

    @Inject(method = "baseTick", at = @At("TAIL"))
    private void updateItemStackHolder(CallbackInfo ci) {
        InventoryUtil.forEachStack(this, stack -> ((EntityLinkedItemStack) stack).apoli$setEntity(this));
    }

}
