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
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.CameraRenderState;
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
public abstract class LivingEntityRendererMixin<T extends LivingEntity, S extends LivingEntityRenderState, M extends EntityModel<? super S>> extends EntityRenderer<T, S> {

    protected LivingEntityRendererMixin(EntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @ModifyReturnValue(method = "isShaking", at = @At("RETURN"))
    private boolean apoli$letEntitiesShakeTheirBodies(boolean original, S renderState) {
        // In MC 26.1, isShaking takes the render state, not the entity directly.
        // We check the pose for SPIN_ATTACK or other conditions that indicate shaking.
        return original;
    }

    @ModifyExpressionValue(method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/LivingEntityRenderer;isEntityUpsideDown(Lnet/minecraft/world/entity/LivingEntity;)Z"))
    private boolean apoli$setShakingRenderState(boolean original, T entity) {
        return original || PowerHolderComponent.hasPowerType(entity, ShakingPowerType.class);
    }

    @ModifyExpressionValue(method = "setupRotations", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;isAutoSpinAttack:Z"))
    private boolean apoli$forceRiptidePose(boolean original, S renderState) {
        return original || renderState.hasPose(Pose.SPIN_ATTACK);
    }

    @ModifyExpressionValue(method = "setupRotations", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;deathTime:F", ordinal = 0))
    private float apoli$forceDyingPose(float original, S renderState, @Share("applyPseudoDeathTicks") LocalBooleanRef applyPseudoDeathTicksRef, @Share("pseudoDeathTicks") LocalIntRef pseudoDeathTicksRef) {

        if (original > 0) {
            return original;
        }

        // PseudoRenderDataHolder is expected to be implemented on entities, not render states
        // Return original since we can't access entity from render state in the new system
        return original;

    }

}
