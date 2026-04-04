package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import io.github.apace100.apoli.access.JumpingEntity;
import io.github.apace100.apoli.access.ModifiableFoodEntity;
import io.github.apace100.apoli.access.ModifiedPoseHolder;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.networking.packet.s2c.DismountPlayerS2CPacket;
import io.github.apace100.apoli.power.type.*;
import io.github.apace100.apoli.util.ActionResultUtil;
import io.github.apace100.apoli.util.InventoryUtil;
import io.github.apace100.apoli.util.PriorityPhase;
import io.github.apace100.apoli.util.modifier.Modifier;
import io.github.apace100.apoli.util.modifier.ModifierUtil;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.entity.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.protocol.game.ClientboundSetHealthPacket;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.Nameable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = Player.class, priority = 999)
public abstract class PlayerEntityMixin extends LivingEntity implements Nameable, CommandOutput, JumpingEntity, ModifiedPoseHolder {

    @Shadow
    public abstract boolean damage(DamageSource source, float amount);

    @Shadow
    public abstract ItemStack getEquippedStack(EquipmentSlot slot);

    @Shadow
    @Final
    Inventory inventory;

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, Level world) {
        super(entityType, world);
    }

    @Inject(method = "getFlyingSpeed", at = @At("RETURN"), cancellable = true)
    private void modifyFlySpeed(CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue(PowerHolderComponent.modify(this, ModifyAirSpeedPowerType.class, cir.getReturnValue()));
    }

    @ModifyVariable(method = "eat", at = @At("HEAD"), argsOnly = true)
    private ItemStack apoli$modifyEatenStack(ItemStack original) {

        SlotAccess newStackRef = InventoryUtil.createStackReference(original);
        ModifiableFoodEntity modifiableFoodEntity = (ModifiableFoodEntity) this;

        List<ModifyFoodPowerType> modifyFoodPowers = PowerHolderComponent.getPowerTypes(this, ModifyFoodPowerType.class)
            .stream()
            .filter(mfp -> mfp.doesApply(original))
            .toList();

        for (ModifyFoodPowerType modifyFoodPower : modifyFoodPowers) {
            modifyFoodPower.setConsumedItemStackReference(newStackRef);
        }

        EdibleItemPowerType.get(original.copy(), this).ifPresent(modifiableFoodEntity::apoli$setEdibleItemPower);

        modifiableFoodEntity.apoli$setCurrentModifyFoodPowers(modifyFoodPowers);
        modifiableFoodEntity.apoli$setOriginalFoodStack(original);

        return newStackRef.get();

    }

    @Unique
    private boolean apoli$updateStatsManually = false;

    @ModifyVariable(method = "eat", at = @At("HEAD"), argsOnly = true)
    private FoodProperties apoli$modifyFoodComponent(FoodProperties original, Level world, ItemStack stack, @Share("modifyFoodPowers") LocalRef<List<ModifyFoodPowerType>> sharedModifyFoodPowers) {

        List<ModifyFoodPowerType> modifyFoodPowers = ((ModifiableFoodEntity) this).apoli$getCurrentModifyFoodPowers()
            .stream()
            .filter(p -> p.doesApply(stack))
            .toList();

        sharedModifyFoodPowers.set(modifyFoodPowers);
        this.apoli$updateStatsManually = false;

        List<Modifier> nutritionModifiers = modifyFoodPowers
            .stream()
            .flatMap(p -> p.getFoodModifiers().stream())
            .toList();
        List<Modifier> saturationModifiers = modifyFoodPowers
            .stream()
            .flatMap(p -> p.getSaturationModifiers().stream())
            .toList();

        int oldNutrition = original.nutrition();
        float oldSaturation = original.saturation();

        int newNutrition = (int) ModifierUtil.applyModifiers(this, nutritionModifiers, oldNutrition);
        float newSaturation = (float) ModifierUtil.applyModifiers(this, saturationModifiers, oldSaturation);

        if (newNutrition != oldNutrition || newSaturation != oldSaturation) {
            this.apoli$updateStatsManually = true;
        }

        return new FoodProperties(
            newNutrition,
            newSaturation,
            original.canAlwaysEat(),
            original.eatSeconds(),
            original.usingConvertsTo(),
            original.effects()
        );

    }

    @Inject(method = "eat", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/FoodData;eat(Lnet/minecraft/world/item/component/FoodProperties;)V", shift = At.Shift.AFTER))
    private void apoli$executeActionsAfterEating(Level world, ItemStack stack, FoodProperties foodComponent, CallbackInfoReturnable<ItemStack> cir, @Share("modifyFoodPowers") LocalRef<List<ModifyFoodPowerType>> sharedModifyFoodPowers) {

        List<ModifyFoodPowerType> modifyFoodPowers = sharedModifyFoodPowers.get();
        if (!((Player) (Object) this instanceof ServerPlayer serverPlayer) || modifyFoodPowers == null) {
            return;
        }

        modifyFoodPowers.forEach(ModifyFoodPowerType::eat);
        if (apoli$updateStatsManually) {
            FoodData hungerManager = serverPlayer.getFoodData();
            serverPlayer.connection.send(new ClientboundSetHealthPacket(this.getHealth(), hungerManager.getFoodLevel(), hungerManager.getSaturationLevel()));
        }

    }

    @Inject(method = "damage", at = @At(value = "RETURN", ordinal = 3), cancellable = true)
    private void allowDamageIfModifyingPowersExist(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {

        boolean hasModifyingPower = false;

        if (source.getEntity() != null) {
            if (source.is(DamageTypeTags.IS_PROJECTILE)) hasModifyingPower = PowerHolderComponent.hasPowerType(source.getEntity(), ModifyProjectileDamagePowerType.class, mpdp -> mpdp.doesApply(source, amount, this));
            else hasModifyingPower = PowerHolderComponent.hasPowerType(source.getEntity(), ModifyDamageDealtPowerType.class, mddp -> mddp.doesApply(source, amount, this));
        }

        hasModifyingPower |= PowerHolderComponent.hasPowerType(this, ModifyDamageTakenPowerType.class, mdtp -> mdtp.doesApply(source, amount));
        if (hasModifyingPower) cir.setReturnValue(super.hurt(source, amount));

    }

    @Inject(method = "removeVehicle", at = @At("HEAD"))
    private void apoli$sendPlayerDismountPacket(CallbackInfo ci) {
        if (this.getVehicle() instanceof ServerPlayer player) {
            ServerPlayNetworking.send(player, new DismountPlayerS2CPacket(this.getId()));
        }
    }

    @Inject(method = "updateSwimming", at = @At("TAIL"))
    private void updateSwimmingPower(CallbackInfo ci) {
        if(PowerHolderComponent.hasPowerType(this, SwimmingPowerType.class)) {
            this.setSwimming(this.isSprinting() && !this.hasVehicle());
            this.touchingWater = this.isSwimming();
            if (this.isSwimming()) {
                this.fallDistance = 0.0F;
                Vec3 look = this.getLookAngle();
                move(MoverType.SELF, new Vec3(look.x/4, look.y/4, look.z/4));
            }
        } else if(PowerHolderComponent.hasPowerType(this, IgnoreWaterPowerType.class)) {
            this.setSwimming(false);
        }
    }

    @Inject(method = "stopSleeping(ZZ)V", at = @At("HEAD"))
    private void invokeWakeUpAction(boolean bl, boolean updateSleepingPlayers, CallbackInfo ci) {
        if(!bl && !updateSleepingPlayers && getSleepingPosition().isPresent()) {
            BlockPos sleepingPos = getSleepingPosition().get();
            PowerHolderComponent.getPowerTypes(this, ActionOnWakeUpPowerType.class).stream().filter(p -> p.doesApply(sleepingPos)).forEach(p -> p.executeActions(sleepingPos, Direction.DOWN));
        }
    }

    // Prevent healing if DisableRegenPower
    // Note that this function was called "shouldHeal" instead of "canFoodHeal" at some point in time.
    @ModifyReturnValue(method = "isHurt", at = @At("RETURN"))
    private boolean apoli$disableFoodRegen(boolean original) {
        return original
            && !PowerHolderComponent.hasPowerType(this, DisableRegenPowerType.class);
    }

    // ModifyExhaustion
    @ModifyVariable(at = @At("HEAD"), method = "causeFoodExhaustion", argsOnly = true)
    private float modifyExhaustion(float exhaustionIn) {
        return PowerHolderComponent.modify(this, ModifyExhaustionPowerType.class, exhaustionIn);
    }

    @Inject(method = "dropAllDeathLoot", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;dropAll()V"))
    private void dropAdditionalInventory(CallbackInfo ci) {
        PowerHolderComponent.withPowerTypes(this, InventoryPowerType.class, InventoryPowerType::shouldDropOnDeath, InventoryPowerType::dropItemsOnDeath);
        PowerHolderComponent.withPowerTypes(this, KeepInventoryPowerType.class, p -> true, KeepInventoryPowerType::preventItemsFromDropping);
    }

    @Inject(method = "dropAllDeathLoot", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;dropAll()V", shift = At.Shift.AFTER))
    private void restoreKeptInventory(CallbackInfo ci) {
        PowerHolderComponent.withPowerTypes(this, KeepInventoryPowerType.class, p -> true, KeepInventoryPowerType::restoreSavedItems);
    }

    @ModifyReturnValue(method = "canEquip", at = @At("RETURN"))
    private boolean apoli$preventArmorDispensing(boolean original, ItemStack stack) {
        return original
            && !PowerHolderComponent.hasPowerType(this, RestrictArmorPowerType.class, p -> p.doesRestrict(stack, this.getPreferredEquipmentSlot(stack)));
    }

    @WrapOperation(method = "interact", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;interact(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;"))
    private InteractionResult apoli$beforeEntityUse(Entity entity, Player player, InteractionHand hand, Operation<InteractionResult> original, @Share("zeroPriority$onEntity") LocalRef<InteractionResult> sharedZeroPriority$onEntity) {

        ItemStack stackInHand = player.getItemInHand(hand);
        for (PreventEntityUsePowerType peup : PowerHolderComponent.getPowerTypes(this, PreventEntityUsePowerType.class)) {

            if (peup.doesApply(entity, hand, stackInHand)) {
                return peup.executeAction(entity, hand);
            }

        }

        for (PreventBeingUsedPowerType pbup : PowerHolderComponent.getPowerTypes(entity, PreventBeingUsedPowerType.class)) {

            if (pbup.doesApply(player, hand, stackInHand)) {
                return pbup.executeAction(player, hand);
            }

        }

        Prioritized.CallInstance<ActiveInteractionPowerType> aipci = new Prioritized.CallInstance<>();

        aipci.add(player, ActionOnEntityUsePowerType.class, p -> p.shouldExecute(entity, hand, stackInHand, PriorityPhase.BEFORE));
        aipci.add(entity, ActionOnBeingUsedPowerType.class, p -> p.shouldExecute(player, hand, stackInHand, PriorityPhase.BEFORE));

        for (int i = aipci.getMaxPriority(); i >= aipci.getMinPriority(); i--) {

            if (!aipci.hasPowerTypes(i)) {
                continue;
            }

            List<ActiveInteractionPowerType> aips = aipci.getPowerTypes(i);
            InteractionResult previousResult = InteractionResult.PASS;

            for (ActiveInteractionPowerType aip : aips) {

                InteractionResult currentResult = InteractionResult.PASS;
                if (aip instanceof ActionOnEntityUsePowerType aoeup) {
                    currentResult = aoeup.executeAction(entity, hand);
                }

                else if (aip instanceof ActionOnBeingUsedPowerType aobup) {
                    currentResult = aobup.executeAction(player, hand);
                }

                if (ActionResultUtil.shouldOverride(previousResult, currentResult)) {
                    previousResult = currentResult;
                }

            }

            if (i == 0) {
                sharedZeroPriority$onEntity.set(previousResult);
                continue;
            }

            if (previousResult == InteractionResult.PASS) {
                continue;
            }

            if (previousResult.shouldSwingHand()) {
                this.swingHand(hand);
            }

            return previousResult;

        }

        return original.call(entity, player, hand);

    }

    @ModifyReturnValue(method = "interact", at = @At("RETURN"), slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getItemInHand(Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/item/ItemStack;", ordinal = 0)))
    private InteractionResult apoli$afterEntityUse(InteractionResult original, Entity entity, InteractionHand hand, @Share("zeroPriority$onEntity") LocalRef<InteractionResult> sharedZeroPriority$onEntity) {

        InteractionResult cachedPriorityZeroResult = sharedZeroPriority$onEntity.get();
        InteractionResult newResult = InteractionResult.PASS;

        if (cachedPriorityZeroResult != null && cachedPriorityZeroResult != InteractionResult.PASS) {
            newResult = cachedPriorityZeroResult;
        }

        else if (original == InteractionResult.PASS) {

            ItemStack stackInHand = this.getItemInHand(hand);
            Prioritized.CallInstance<ActiveInteractionPowerType> aipci = new Prioritized.CallInstance<>();

            aipci.add(this, ActionOnEntityUsePowerType.class, p -> p.shouldExecute(entity, hand, stackInHand, PriorityPhase.AFTER));
            aipci.add(entity, ActionOnBeingUsedPowerType.class, p -> p.shouldExecute((Player) (Object) this, hand, stackInHand, PriorityPhase.AFTER));

            for (int i = aipci.getMaxPriority(); i >= aipci.getMinPriority(); i--) {

                if (!aipci.hasPowerTypes(i)) {
                    continue;
                }

                List<ActiveInteractionPowerType> aips = aipci.getPowerTypes(i);
                InteractionResult previousResult = InteractionResult.PASS;

                for (ActiveInteractionPowerType aip : aips) {

                    InteractionResult currentResult = InteractionResult.PASS;
                    if (aip instanceof ActionOnEntityUsePowerType aoeup) {
                        currentResult = aoeup.executeAction(entity, hand);
                    }

                    else if (aip instanceof ActionOnBeingUsedPowerType aobup) {
                        currentResult = aobup.executeAction((Player) (Object) this, hand);
                    }

                    if (ActionResultUtil.shouldOverride(previousResult, currentResult)) {
                        previousResult = currentResult;
                    }

                }

                if (previousResult != InteractionResult.PASS) {
                    newResult = previousResult;
                    break;
                }

            }

        }

        if (newResult.shouldSwingHand()) {
            this.swingHand(hand);
        }

        return ActionResultUtil.shouldOverride(original, newResult)
            ? newResult
            : original;

    }

    @ModifyExpressionValue(method = "jump", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;isSprinting()Z"))
    private boolean apoli$shouldApplySprintJumpExhaustion(boolean original) {
        return original && this.apoli$applySprintJumpEffects();
    }

    @WrapWithCondition(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;updatePose()V"))
    private boolean apoli$preventUpdatingPose(Player player) {
        return this.apoli$getModifiedEntityPose().isEmpty();
    }

}
