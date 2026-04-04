package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.github.apace100.apoli.power.type.EdibleItemPowerType;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.item.ItemInHandRenderer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionHand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemInHandRenderer.class)
public abstract class HeldItemRendererMixin {

    @ModifyExpressionValue(method = "renderHandsWithItems", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z"))
    private boolean apoli$overrideSpecialTransforms(boolean original, AbstractClientPlayer player, float tickDelta, float pitch, InteractionHand hand, float swingProgress, ItemStack stack) {
        return original && EdibleItemPowerType.get(stack)
            .map(EdibleItemPowerType::getFoodComponent)
            .map(fc -> player.canConsume(fc.canAlwaysEat()))
            .orElse(true);
    }

}
