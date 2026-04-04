package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.authlib.GameProfile;
import io.github.apace100.apoli.access.CustomToastViewer;
import io.github.apace100.apoli.access.PowerCraftingObject;
import io.github.apace100.apoli.access.WaterMovingEntity;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.data.CustomToastData;
import io.github.apace100.apoli.power.type.*;
import io.github.apace100.apoli.screen.toast.CustomToast;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.recipebook.ClientRecipeBook;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.stats.StatsCounter;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.function.Predicate;

@Mixin(LocalPlayer.class)
public abstract class ClientPlayerEntityMixin extends AbstractClientPlayer implements WaterMovingEntity, CustomToastViewer {

    @Unique
    private boolean apoli$isMoving = false;

    @Shadow
    @Final
    protected Minecraft client;

    @Shadow
    protected abstract boolean isWalking();

    @Shadow @Final private ClientRecipeBook recipeBook;

    private ClientPlayerEntityMixin(ClientLevel world, GameProfile profile) {
        super(world, profile);
    }

    @Inject(at = @At("HEAD"), method = "isUnderWater", cancellable = true)
    private void allowSwimming(CallbackInfoReturnable<Boolean> cir)  {
        if(PowerHolderComponent.hasPowerType(this, SwimmingPowerType.class)) {
            cir.setReturnValue(true);
        } else if(PowerHolderComponent.hasPowerType(this, IgnoreWaterPowerType.class)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(at = @At("HEAD"), method = "aiStep")
    private void beginMovementPhase(CallbackInfo ci) {
        apoli$isMoving = true;
    }

    @Inject(at = @At("TAIL"), method = "aiStep")
    private void endMovementPhase(CallbackInfo ci) {
        apoli$isMoving = false;
    }

    @Override
    public boolean apoli$isInMovementPhase() {
        return apoli$isMoving;
    }

    @Redirect(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Abilities;getFlySpeed()F"))
    private float modifyFlySpeed(Abilities playerAbilities){
        return PowerHolderComponent.modify(this, ModifyAirSpeedPowerType.class, playerAbilities.getFlySpeed());
    }

    @Override
    public void apoli$showToast(CustomToastData toastData) {
        Minecraft client = Minecraft.getInstance();
        client.execute(() -> {
            CustomToast toast = new CustomToast(toastData);
            client.getToastManager().add(toast);
        });
    }

    @Inject(method = "aiStep", at = @At("HEAD"))
    private void apoli$cacheSprintingPowers(CallbackInfo ci, @Share("sprintingPowers") LocalRef<List<SprintingPowerType>> sprintingPowersRef, @Share("preventSprinting") LocalBooleanRef preventSprintingRef) {
        sprintingPowersRef.set(PowerHolderComponent.getPowerTypes(this, SprintingPowerType.class));
        preventSprintingRef.set(PowerHolderComponent.hasPowerType(this, PreventSprintingPowerType.class));
    }

    @ModifyExpressionValue(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/LocalPlayer;canStartSprinting()Z"))
    private boolean apoli$allowActivePowerSprinting(boolean original, @Share("sprintingPowers") LocalRef<List<SprintingPowerType>> sprintingPowersRef, @Share("preventSprinting") LocalBooleanRef preventSprintingRef) {
        return original || (this.isWalking() && sprintingPowersRef.get()
            .stream()
            .anyMatch(SprintingPowerType::shouldRequireInput));
    }

    @Inject(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/LocalPlayer;isSprinting()Z"))
    private void apoli$allowPassivePowerSprinting(CallbackInfo ci, @Share("sprintingPowers") LocalRef<List<SprintingPowerType>> sprintingPowersRef, @Share("preventSprinting") LocalBooleanRef preventSprintingRef) {

        if (this.isSprinting() || preventSprintingRef.get()) {
            return;
        }

        this.setSprinting(sprintingPowersRef.get()
            .stream()
            .anyMatch(Predicate.not(SprintingPowerType::shouldRequireInput)));

    }

    @ModifyExpressionValue(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/LocalPlayer;canSprint()Z"))
    private boolean apoli$accountForSprintingPowersWhenCancelling(boolean original, @Share("sprintingPowers") LocalRef<List<SprintingPowerType>> sprintingPowersRef, @Share("preventSprinting") LocalBooleanRef preventSprintingRef) {
        return (original || !sprintingPowersRef.get().isEmpty())
            && !preventSprintingRef.get();
    }

    @ModifyExpressionValue(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/LocalPlayer;isShiftKeyDown()Z"))
    private boolean apoli$forceSneakingPose(boolean original) {
        return original || PosePowerType.hasEntityPose(this, Pose.CROUCHING);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void apoli$cachePlayerToRecipeBook(Minecraft client, ClientLevel world, ClientPacketListener networkHandler, StatsCounter stats, ClientRecipeBook recipeBook, boolean lastSneaking, boolean lastSprinting, CallbackInfo ci) {

        if (this.recipeBook instanceof PowerCraftingObject pco) {
            pco.apoli$setPlayer(this);
        }

    }

}
