package io.github.apace100.apoli.mixin.power.type;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.type.ElytraFlightPowerType;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.WingsLayer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(WingsLayer.class)
public abstract class ElytraFlightPowerTypeMixin {

	// TODO: MC 26.1 - WingsLayer.render() is now submit() and takes HumanoidRenderState instead of LivingEntity.
	// The rendering pipeline changed from entity-based to render-state-based.
	// These injections need to be reimplemented using the new render state API.
	// @ModifyExpressionValue(method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/HumanoidRenderState;FF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z"))
	// private boolean apoli$wearingElytraProxy(boolean original, PoseStack matrices, MultiBufferSource vertexConsumerProvider, int i, LivingEntity entity) {
	// 	return original
	// 		|| PowerHolderComponent.hasPowerType(entity, ElytraFlightPowerType.class, ElytraFlightPowerType::shouldRenderElytra);
	// }

	// @WrapOperation(method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/HumanoidRenderState;FF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/rendertype/RenderType;getArmorCutoutNoCull(Lnet/minecraft/resources/Identifier;)Lnet/minecraft/client/renderer/rendertype/RenderType;"))
	// private RenderType apoli$overrideElytraTexture(Identifier texture, Operation<RenderType> original, PoseStack matrices, MultiBufferSource vertexConsumerProvider, int i, LivingEntity entity) {
	// 	return original.call(PowerHolderComponent.getPowerTypes(entity, ElytraFlightPowerType.class)
	// 		.stream()
	// 		.findFirst()
	// 		.flatMap(ElytraFlightPowerType::getTextureLocation)
	// 		.orElse(texture));
	// }

}
