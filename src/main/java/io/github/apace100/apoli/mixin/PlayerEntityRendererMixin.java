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
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.entity.Pose;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Environment(EnvType.CLIENT)
@Mixin(AvatarRenderer.class)
public abstract class PlayerEntityRendererMixin extends LivingEntityRenderer<AbstractClientPlayer, AvatarRenderState, PlayerModel> {

    private PlayerEntityRendererMixin(EntityRendererProvider.Context ctx, PlayerModel model, float shadowRadius) {
        super(ctx, model, shadowRadius);
    }

    @WrapOperation(method = "renderPlayerArm", at = {@At(value = "INVOKE", target = "Lnet/minecraft/client/model/geom/ModelPart;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;II)V", ordinal = 0), @At(value = "INVOKE", target = "Lnet/minecraft/client/model/geom/ModelPart;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;II)V", ordinal = 1)})
    private void apoli$makeArmAndSleeveTransparent(ModelPart instance, PoseStack matrices, VertexConsumer vertices, int light, int overlay, Operation<Void> original, PoseStack mMatrices, MultiBufferSource mVertexConsumers, int mLight, AbstractClientPlayer mPlayer, @Local Identifier skinTextureId) {

        List<ModelColorPowerType> modelColorPowers = PowerHolderComponent.getPowerTypes(mPlayer, ModelColorPowerType.class);
        if (modelColorPowers.isEmpty()) {
            original.call(instance, matrices, vertices, light, overlay);
            return;
        }

        float red = modelColorPowers.stream().map(ModelColorPowerType::getRed).reduce((a, b) -> a * b).orElse(1.0f);
        float green = modelColorPowers.stream().map(ModelColorPowerType::getGreen).reduce((a, b) -> a * b).orElse(1.0f);
        float blue = modelColorPowers.stream().map(ModelColorPowerType::getBlue).reduce((a, b) -> a * b).orElse(1.0f);
        float alpha = modelColorPowers.stream().map(ModelColorPowerType::getAlpha).min(Float::compare).orElse(1.0f);

        int packedArgb = ((int)(alpha * 255) << 24) | ((int)(red * 255) << 16) | ((int)(green * 255) << 8) | (int)(blue * 255);
        instance.render(matrices, mVertexConsumers.getBuffer(RenderTypes.entityTranslucent(skinTextureId)), light, overlay, packedArgb);

    }

    // TODO: MC 26.1 changed AvatarRenderer.setupRotations signature from
    // (AbstractClientPlayer, PoseStack, FFFF) to (AvatarRenderState, PoseStack, FF).
    // The entity data (isFallFlying, isUsingRiptide, getFallFlyingTicks) is now accessed
    // through render state fields, not entity method calls. These need reimplementing.
    // @ModifyExpressionValue(method = "setupRotations(Lnet/minecraft/client/multiplayer/AbstractClientPlayer;Lcom/mojang/blaze3d/vertex/PoseStack;FFFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/AbstractClientPlayer;isFallFlying()Z"))
    // private boolean apoli$forceFallFlyingPose(boolean original, AbstractClientPlayer player, ...) { ... }

    // @ModifyExpressionValue(method = "setupRotations(Lnet/minecraft/client/multiplayer/AbstractClientPlayer;Lcom/mojang/blaze3d/vertex/PoseStack;FFFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/AbstractClientPlayer;isUsingRiptide()Z"))
    // private boolean apoli$accountForForcedRiptide(boolean original, AbstractClientPlayer player) { ... }

    // @ModifyExpressionValue(method = "setupRotations(Lnet/minecraft/client/multiplayer/AbstractClientPlayer;Lcom/mojang/blaze3d/vertex/PoseStack;FFFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/AbstractClientPlayer;getFallFlyingTicks()I"))
    // private int apoli$applyPseudoFallFlyingTicks(int original, AbstractClientPlayer player, ...) { ... }

    @ModifyReturnValue(method = "getArmPose", at = @At("RETURN"))
    private static HumanoidModel.ArmPose apoli$overrideArmPose(HumanoidModel.ArmPose original, AbstractClientPlayer player) {
        return ArmPoseReference
            .getArmPose(player)
            .orElse(original);
    }

}
