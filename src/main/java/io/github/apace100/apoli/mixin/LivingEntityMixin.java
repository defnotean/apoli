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
    public abstract float getMovementSpeed();

    @Shadow
    private Optional<BlockPos> climbingPos;

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

    @Inject(method = "onEffectRemoved", at = @At("TAIL"))
    private void apoli$updateStatusEffectWhenRemoved(MobEffectInstance effectInstance, CallbackInfo ci) {
        SyncStatusEffectsUtil.sendStatusEffectUpdatePacket((LivingEntity) (Object) this, SyncStatusEffectsUtil.UpdateType.REMOVE, effectInstance);
    }

    @Inject(method = "removeAllEffects", at = @At("RETURN"))
    private void apoli$updateStatusEffectWhenCleared(CallbackInfoReturnable<Boolean> cir) {
        SyncStatusEffectsUtil.sendStatusEffectUpdatePacket((LivingEntity) (Object) this, SyncStatusEffectsUtil.UpdateType.CLEAR, null);
    }

    @ModifyVariable(method = "addEffect(Lnet/minecraft/world/entity/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z", at = @At("HEAD"), argsOnly = true)
    private MobEffectInstance apoli$modifyStatusEffect(MobEffectInstance original) {

        Holder<MobEffect> effectType = original.getEffectType();

        float amplifier = PowerHolderComponent.modify(this, ModifyStatusEffectAmplifierPowerType.class, original.getAmplifier(), p -> p.doesApply(effectType));
        float duration = PowerHolderComponent.modify(this, ModifyStatusEffectDurationPowerType.class, original.getDuration(), p -> p.doesApply(effectType));

        return new MobEffectInstance(
            effectType,
            Math.round(duration),
            Math.round(amplifier),
            original.isAmbient(),
            original.shouldShowParticles(),
            original.shouldShowIcon(),
            ((HiddenEffectStatus) original).apoli$getHiddenEffect()
        );

    }

    @Inject(method = "setAttacker", at = @At("TAIL"))
    private void apoli$syncAttacker(LivingEntity attacker, CallbackInfo ci) {

        if (this.level().isClientSide) {
            return;
        }

        Optional<Integer> attackerId = Optional.ofNullable(this.attacker).map(Entity::getId);
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

    @ModifyExpressionValue(method = "onDamageTaken", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/damagesource/DamageSources;generic()Lnet/minecraft/world/damagesource/DamageSource;"))
    private DamageSource apoli$overrideDamageSourceOnSync(DamageSource original, DamageSource source) {
        return this.damageSources().create(ApoliDamageTypes.SYNC_DAMAGE_SOURCE);
    }

    @ModifyVariable(method = "damage", at = @At("HEAD"), argsOnly = true)
    private float apoli$modifyDamageTaken(float original, DamageSource source, float amount) {

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
            this.damageArmor(source, amount);
        }

        return apoli$shouldApplyArmor
            .map(result -> !result)
            .orElse(original);

    }

    @WrapWithCondition(method = "getDamageAfterArmorAbsorb", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;damageArmor(Lnet/minecraft/world/damagesource/DamageSource;F)V"))
    private boolean apoli$allowDamagingArmor(LivingEntity instance, DamageSource source, float amount) {
        return apoli$shouldDamageArmor.orElse(true);
    }

    @ModifyExpressionValue(method = "getDamageAfterArmorAbsorb", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/DamageUtil;getDamageLeft(Lnet/minecraft/world/entity/LivingEntity;FLnet/minecraft/world/damagesource/DamageSource;FF)F"))
    private float apoli$allowApplyingArmor(float modified, DamageSource source, float original) {
        return apoli$shouldApplyArmor.orElse(true)
            ? modified
            : original;
    }

    @ModifyExpressionValue(method = "damage", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isDead()Z", ordinal = 0))
    private boolean apoli$preventHitIfDamageIsZero(boolean original, DamageSource source, float amount) {
        return original || apoli$hasModifiedDamage && amount <= 0.0F;
    }

    @Inject(method = "damage", at = @At("RETURN"), slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isSleeping()Z")))
    private void apoli$invokeHitActions(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {

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

    @Inject(method = "damage", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;onDeath(Lnet/minecraft/world/damagesource/DamageSource;)V"))
    private void invokeDeathAction(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        PowerHolderComponent.withPowerTypes(this, ActionOnDeathPowerType.class, p -> p.doesApply(source.getEntity(), source, amount), p -> p.onDeath(source.getEntity()));
    }

    @Inject(method = "damage", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;onDeath(Lnet/minecraft/world/damagesource/DamageSource;)V"))
    private void invokeKillAction(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
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

    @Inject(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getFrozenTicks()I"))
    private void freezeEntityFromPower(CallbackInfo ci) {
        if(PowerHolderComponent.hasPowerType(this, FreezePowerType.class)) {
            this.prevPowderSnowState = this.inPowderSnow;
            this.inPowderSnow = true;
        }
    }

    @Inject(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;removePowderSnowSlow()V"))
    private void unfreezeEntityFromPower(CallbackInfo ci) {
        if(PowerHolderComponent.hasPowerType(this, FreezePowerType.class)) {
            this.inPowderSnow = this.prevPowderSnowState;
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

    @ModifyExpressionValue(method = "jump", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isSprinting()Z"))
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

        this.climbingPos = Optional.of(this.getBlockPos());
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
    @ModifyVariable(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/Level;getFluidState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/material/FluidState;"), method = "travel", name = "d", ordinal = 0)
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

    @ModifyVariable(method = "travel", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;onGround()Z", opcode = Opcodes.GETFIELD, ordinal = 2))
    private float modifySlipperiness(float original) {
        return PowerHolderComponent.modify(this, ModifySlipperinessPowerType.class, original, p -> p.doesApply(getWorld(), getVelocityAffectingPos()));
    }

    @ModifyExpressionValue(method = "damage", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isDead()Z", ordinal = 1))
    private boolean apoli$preventDeath(boolean original, DamageSource source, float amount) {

        if (original && PreventDeathPowerType.doesPrevent(this, source, amount)) {
            this.setHealth(1.0F);
            return false;
        }

        return original;

    }

    @ModifyVariable(method = "eat", at = @At("HEAD"), argsOnly = true)
    private ItemStack apoli$modifyEatenStack(ItemStack original) {

        LivingEntity thisAsLiving = (LivingEntity) (Object) this;
        if (thisAsLiving instanceof Player) {
            return original;
        }

        SlotAccess newStackRef = InventoryUtil.createStackReference(original);
        List<ModifyFoodPowerType> modifyFoodPowers = PowerHolderComponent.getPowerTypes(this, ModifyFoodPowerType.class)
            .stream()
            .filter(mfp -> mfp.doesApply(original))
            .toList();

        for (ModifyFoodPowerType modifyFoodPower : modifyFoodPowers) {
            modifyFoodPower.setConsumedItemStackReference(newStackRef);
        }

        EdibleItemPowerType.get(original.copy(), this).ifPresent(this::apoli$setEdibleItemPower);

        this.apoli$setCurrentModifyFoodPowers(modifyFoodPowers);
        this.apoli$setOriginalFoodStack(original);

        return newStackRef.get();

    }

    @ModifyVariable(method = "eat", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;applyEffectsFromMobEffect(Lnet/minecraft/world/item/component/FoodProperties;)V", shift = At.Shift.AFTER), argsOnly = true)
    private ItemStack apoli$restoreOriginalEatenStack(ItemStack modified) {
        ItemStack original = this.apoli$getOriginalFoodStack();
        return original == null
            ? modified
            : original;
    }

    @ModifyReturnValue(method = "eat", at = @At("RETURN"))
    private ItemStack apoli$modifyCustomFoodAndCleanUp(ItemStack original) {

        EdibleItemPowerType edibleItemPower = this.apoli$getEdibleItemPower();
        ItemStack result = original;

        modifyCustomFood:
        if (edibleItemPower != null) {

            edibleItemPower.executeEntityAction();

            SlotAccess newStackRef = InventoryUtil.createStackReference(original);
            SlotAccess resultStackRef = edibleItemPower.executeItemActions(newStackRef);

            ItemStack newStack = newStackRef.get();
            ItemStack resultStack = resultStackRef.get();

            if (resultStackRef == null) {
                result = newStack;
                break modifyCustomFood;
            }

            else if (newStack.isEmpty()) {
                result = resultStack;
                break modifyCustomFood;
            }

            else if (ItemStack.matches(resultStack, newStack)) {
                newStack.grow(1);
            }

            else if ((LivingEntity) (Object) this instanceof Player player && !player.isCreative()) {
                player.getInventory().addItem(resultStack);
            }

            else {
                InventoryUtil.throwItem(this, resultStack, false, false);
            }

            result = newStack;

        }

        this.apoli$setCurrentModifyFoodPowers(new LinkedList<>());
        this.apoli$setOriginalFoodStack(null);
        this.apoli$setEdibleItemPower(null);

        return result;

    }

    @Inject(method = "applyEffectsFromMobEffect", at = @At("HEAD"), cancellable = true)
    private void apoli$preventApplyingFoodEffects(FoodProperties component, CallbackInfo ci) {
        if (this.apoli$getCurrentModifyFoodPowers().stream().anyMatch(ModifyFoodPowerType::doesPreventEffects)) {
            ci.cancel();
        }
    }

    @Shadow @Nullable private LivingEntity attacker;

    @Shadow public abstract void damageArmor(DamageSource source, float amount);

    @Shadow public abstract int getArmor();

    @Shadow public abstract AttributeMap getAttributes();

    @Shadow public abstract boolean onClimbable();

    @Shadow public abstract boolean isDead();

    @Shadow public abstract double getAttributeValue(Holder<Attribute> attribute);

    @Shadow public abstract float getArmorVisibility();

    @Shadow public abstract boolean damage(DamageSource source, float amount);

    @Shadow protected abstract void onEffectRemoved(MobEffectInstance effect);

    @Shadow public float sidewaysSpeed;

    @Shadow public float forwardSpeed;

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

    @ModifyExpressionValue(method = "jump", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isSprinting()Z"))
    private boolean apoli$cancelOutJumpVelocityIfNotMovingWithSprintPower(boolean original) {
        // The movement check is here so this doesn't happen if the player is moving at a sprinting amount.
        if (PowerHolderComponent.hasPowerType(this, SprintingPowerType.class) && this.apoli$getHorizontalMovementValue() < this.getAttributeValue(Attributes.GENERIC_MOVEMENT_SPEED)) {
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
