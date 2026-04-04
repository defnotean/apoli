package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import io.github.apace100.apoli.access.PseudoRenderDataHolder;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.type.ModelColorPowerType;
import io.github.apace100.apoli.power.type.PosePowerType;
import io.github.apace100.apoli.util.ArmPoseReference;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererFactory;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.model.PlayerEntityModel;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.entity.Pose;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.ColorHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Environment(EnvType.CLIENT)
@Mixin(PlayerRenderer.class)
public abstract class PlayerEntityRendererMixin extends LivingEntityRenderer<AbstractClientPlayer, PlayerEntityModel<AbstractClientPlayer>> {

    private PlayerEntityRendererMixin(EntityRendererFactory.Context ctx, PlayerEntityModel<AbstractClientPlayer> model, float shadowRadius) {
        super(ctx, model, shadowRadius);
    }

    @WrapOperation(method = "renderPlayerArm", at = {@At(value = "INVOKE", target = "Lnet/minecraft/client/model/ModelPart;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/VertexConsumer;II)V", ordinal = 0), @At(value = "INVOKE", target = "Lnet/minecraft/client/model/ModelPart;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/VertexConsumer;II)V", ordinal = 1)})
    private void apoli$makeArmAndSleeveTransparent(ModelPart instance, PoseStack matrices, VertexConsumer vertices, int light, int overlay, Operation<Void> original, PoseStack mMatrices, MultiBufferSource mVertexConsumers, int mLight, AbstractClientPlayer mPlayer, @Local ResourceLocation skinTextureId) {

        List<ModelColorPowerType> modelColorPowers = PowerHolderComponent.getPowerTypes(mPlayer, ModelColorPowerType.class);
        if (modelColorPowers.isEmpty()) {
            original.call(instance, matrices, vertices, light, overlay);
            return;
        }

        float red = modelColorPowers.stream().map(ModelColorPowerType::getRed).reduce((a, b) -> a * b).orElse(1.0f);
        float green = modelColorPowers.stream().map(ModelColorPowerType::getGreen).reduce((a, b) -> a * b).orElse(1.0f);
        float blue = modelColorPowers.stream().map(ModelColorPowerType::getBlue).reduce((a, b) -> a * b).orElse(1.0f);
        float alpha = modelColorPowers.stream().map(ModelColorPowerType::getAlpha).min(Float::compare).orElse(1.0f);

        instance.render(matrices, mVertexConsumers.getBuffer(RenderType.getEntityTranslucent(skinTextureId)), light, overlay, ColorHelper.Argb.fromFloats(alpha, red, green, blue));

    }

    @ModifyExpressionValue(method = "setupRotations(Lnet/minecraft/client/multiplayer/AbstractClientPlayer;Lcom/mojang/blaze3d/vertex/PoseStack;FFFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/AbstractClientPlayer;isFallFlying()Z"))
    private boolean apoli$forceFallFlyingPose(boolean original, AbstractClientPlayer player, @Share("applyPseudoFallFlyingTicks") LocalBooleanRef applyPseudoFallFlyingTicksRef, @Share("pseudoRoll") LocalIntRef pseudoRollRef) {

        if (original || !(player instanceof PseudoRenderDataHolder renderData)) {
            return original;
        }

        int pseudoRoll = renderData.apoli$getPseudoFallFlyingTicks();
        boolean apply = pseudoRoll > 0;

        pseudoRollRef.set(pseudoRoll);
        applyPseudoFallFlyingTicksRef.set(apply);

        return apply;

    }

    @ModifyExpressionValue(method = "setupRotations(Lnet/minecraft/client/multiplayer/AbstractClientPlayer;Lcom/mojang/blaze3d/vertex/PoseStack;FFFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/AbstractClientPlayer;isUsingRiptide()Z"))
    private boolean apoli$accountForForcedRiptide(boolean original, AbstractClientPlayer player) {
        return original || PosePowerType.hasEntityPose(player, Pose.SPIN_ATTACK);
    }

    @ModifyExpressionValue(method = "setupRotations(Lnet/minecraft/client/multiplayer/AbstractClientPlayer;Lcom/mojang/blaze3d/vertex/PoseStack;FFFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/AbstractClientPlayer;getFallFlyingTicks()I"))
    private int apoli$applyPseudoFallFlyingTicks(int original, AbstractClientPlayer player, @Share("applyPseudoFallFlyingTicks") LocalBooleanRef applyPseudoFallFlyingTicksRef, @Share("pseudoRoll") LocalIntRef pseudoRollRef) {
        return applyPseudoFallFlyingTicksRef.get()
            ? pseudoRollRef.get()
            : original;
    }

    @ModifyReturnValue(method = "getArmPose", at = @At("RETURN"))
    private static HumanoidModel.ArmPose apoli$overrideArmPose(HumanoidModel.ArmPose original, AbstractClientPlayer player) {
        return ArmPoseReference
            .getArmPose(player)
            .orElse(original);
    }

}
