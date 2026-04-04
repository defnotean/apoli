package io.github.apace100.apoli.mixin.power.type;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.type.ElytraFlightPowerType;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ElytraLayer.class)
public abstract class ElytraFlightPowerTypeMixin {

	@ModifyExpressionValue(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z"))
	private boolean apoli$wearingElytraProxy(boolean original, PoseStack matrices, MultiBufferSource vertexConsumerProvider, int i, LivingEntity entity) {
		return original
			|| PowerHolderComponent.hasPowerType(entity, ElytraFlightPowerType.class, ElytraFlightPowerType::shouldRenderElytra);
	}

	@WrapOperation(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderType;getArmorCutoutNoCull(Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/client/renderer/RenderType;"))
	private RenderType apoli$overrideElytraTexture(ResourceLocation texture, Operation<RenderType> original, PoseStack matrices, MultiBufferSource vertexConsumerProvider, int i, LivingEntity entity) {
		return original.call(PowerHolderComponent.getPowerTypes(entity, ElytraFlightPowerType.class)
			.stream()
			.findFirst()
			.flatMap(ElytraFlightPowerType::getTextureLocation)
			.orElse(texture));
	}

}
