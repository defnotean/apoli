package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.github.apace100.apoli.util.ArmPoseReference;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.model.AnimalModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.model.ModelWithArms;
import net.minecraft.client.renderer.entity.model.ModelWithHead;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Environment(EnvType.CLIENT)
@Mixin(HumanoidModel.class)
public abstract class BipedEntityModelMixin<T extends LivingEntity> extends AnimalModel<T> implements ModelWithArms, ModelWithHead {

    @ModifyExpressionValue(method = "positionRightArm", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/entity/model/HumanoidModel;rightArmPose:Lnet/minecraft/client/renderer/entity/model/HumanoidModel$ArmPose;"))
    private HumanoidModel.ArmPose apoli$overrideRightArmPose(HumanoidModel.ArmPose original, T entity) {
        return ArmPoseReference
            .getArmPose(entity)
            .orElse(original);
    }

    @ModifyExpressionValue(method = "positionLeftArm", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/entity/model/HumanoidModel;leftArmPose:Lnet/minecraft/client/renderer/entity/model/HumanoidModel$ArmPose;"))
    private HumanoidModel.ArmPose apoli$overrideLeftArmPose(HumanoidModel.ArmPose original, T entity) {
        return ArmPoseReference
            .getArmPose(entity)
            .orElse(original);
    }

}
