package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import io.github.apace100.apoli.access.PseudoRenderDataHolder;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.type.*;
import net.minecraft.client.renderer.rendertype.RenderType;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.model.EntityModel;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;
import java.util.function.Predicate;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin extends EntityRenderer<LivingEntity, LivingEntityRenderState> {

    protected LivingEntityRendererMixin(EntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @ModifyReturnValue(method = "isShaking", at = @At("RETURN"))
    private boolean apoli$letEntitiesShakeTheirBodies(boolean original, LivingEntity entity) {
        return original || PowerHolderComponent.hasPowerType(entity, ShakingPowerType.class);
    }

    @ModifyExpressionValue(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;hasOutline(Lnet/minecraft/world/entity/Entity;)Z"))
    private boolean apoli$preventOutlineWhenInvisible(boolean original, LivingEntity entity) {
        return !PowerHolderComponent.hasPowerType(entity, InvisibilityPowerType.class, Predicate.not(InvisibilityPowerType::shouldRenderOutline)) && original;
    }

    @WrapOperation(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/LivingEntityRenderer;getRenderLayer(Lnet/minecraft/world/entity/LivingEntity;ZZZ)Lnet/minecraft/client/renderer/RenderType;"))
    private RenderType apoli$useTranslucentRenderLayerWhenVisible(LivingEntityRenderer<?, ?> renderer, LivingEntity entity, boolean showBody, boolean translucent, boolean showOutline, Operation<RenderType> original) {
        return original.call(renderer, entity, showBody, translucent || showBody && PowerHolderComponent.hasPowerType(entity, ModelColorPowerType.class, ModelColorPowerType::isTranslucent), showOutline);
    }

    @WrapWithCondition(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/layers/RenderLayer;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/Entity;FFFFFF)V"))
    private boolean apoli$preventFeatureRender(RenderLayer<?, ?> instance, PoseStack matrices, MultiBufferSource vertexConsumers, int light, Entity entity, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
        return (!(instance instanceof HumanoidArmorLayer<?, ?, ?>) || !PowerHolderComponent.hasPowerType(entity, InvisibilityPowerType.class, Predicate.not(InvisibilityPowerType::shouldRenderArmor)))
            && !PowerHolderComponent.hasPowerType(entity, PreventFeatureRenderPowerType.class, p -> p.doesApply(instance));
    }

    @WrapOperation(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/EntityModel;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V"))
    private void apoli$renderColorChangedModel(EntityModel<LivingEntity> entityModel, PoseStack matrixStack, VertexConsumer vertexConsumer, int light, int overlay, int argb, Operation<Void> original, LivingEntity entity) {

        List<ModelColorPowerType> modelColorPowers = PowerHolderComponent.getPowerTypes(entity, ModelColorPowerType.class);
        if (modelColorPowers.isEmpty()) {
            original.call(entityModel, matrixStack, vertexConsumer, light, overlay, argb);
            return;
        }

        //  TODO: Implement custom blending modes for blending colors -eggohito
        float newRed = modelColorPowers
            .stream()
            .map(ModelColorPowerType::getRed)
            .reduce((float) ((argb >> 16) & 0xFF) / 255, (a, b) -> a * b);
        float newGreen = modelColorPowers
            .stream()
            .map(ModelColorPowerType::getGreen)
            .reduce((float) ((argb >> 8) & 0xFF) / 255, (a, b) -> a * b);
        float newBlue = modelColorPowers
            .stream()
            .map(ModelColorPowerType::getBlue)
            .reduce((float) (argb & 0xFF) / 255, (a, b) -> a * b);

        float oldAlpha = (float) ((argb >> 24) & 0xFF) / 255;
        float newAlpha = modelColorPowers
            .stream()
            .map(ModelColorPowerType::getAlpha)
            .min(Float::compareTo)
            .map(alphaFactor -> oldAlpha * alphaFactor)
            .orElse(oldAlpha);

        int packedArgb = ((int)(newAlpha * 255) << 24) | ((int)(newRed * 255) << 16) | ((int)(newGreen * 255) << 8) | (int)(newBlue * 255);
        original.call(entityModel, matrixStack, vertexConsumer, light, overlay, packedArgb);

    }

    @ModifyExpressionValue(method = "setupRotations", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isUsingRiptide()Z"))
    private boolean apoli$forceRiptidePose(boolean original, LivingEntity entity) {
        return original || PosePowerType.hasEntityPose(entity, Pose.SPIN_ATTACK);
    }

    @ModifyExpressionValue(method = "setupRotations", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/LivingEntity;deathTime:I", ordinal = 0))
    private int apoli$forceDyingPose(int original, LivingEntity entity, @Share("applyPseudoDeathTicks") LocalBooleanRef applyPseudoDeathTicksRef, @Share("pseudoDeathTicks") LocalIntRef pseudoDeathTicksRef) {

        if (original > 0 || !(entity instanceof PseudoRenderDataHolder renderData)) {
            return original;
        }

        int pseudoDeathTicks = renderData.apoli$getPseudoDeathTicks();

        pseudoDeathTicksRef.set(pseudoDeathTicks);
        applyPseudoDeathTicksRef.set(pseudoDeathTicks > 0);

        return pseudoDeathTicks;

    }

    @ModifyExpressionValue(method = "setupRotations", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/LivingEntity;deathTime:I", ordinal = 1))
    private int apoli$applyPseudoDeathTicks(int original, LivingEntity entity, @Share("applyPseudoDeathTicks") LocalBooleanRef applyPseudoDeathTicksRef, @Share("pseudoDeathTicks") LocalIntRef pseudoDeathTicksRef) {
        return applyPseudoDeathTicksRef.get()
            ? pseudoDeathTicksRef.get()
            : original;
    }

}
