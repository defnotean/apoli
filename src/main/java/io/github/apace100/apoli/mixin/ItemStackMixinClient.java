package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.ApoliClient;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.component.item.ApoliDataComponentTypes;
import io.github.apace100.apoli.component.item.ItemPowersComponent;
import io.github.apace100.apoli.power.type.PreventItemUsePowerType;
import io.github.apace100.apoli.power.type.TooltipPowerType;
import io.github.apace100.apoli.util.ApoliConfigClient;
import io.github.apace100.apoli.util.keybinding.KeyBindingUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.world.item.ItemUseAnimation;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;
import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
@Mixin(ItemStack.class)
public abstract class ItemStackMixinClient implements DataComponentHolder {

    @Shadow
    public abstract ItemUseAnimation getUseAnimation();

    @Shadow
    public abstract DataComponentMap getComponents();

    @Shadow
    public abstract Item getItem();

    @Unique
    private EnumSet<EquipmentSlotGroup> apoli$appendedSlots;

    @Unique
    private Item.TooltipContext apoli$tooltipContext;

    @Unique
    private TooltipFlag apoli$tooltipType;

    @Unique
    private List<Component> apoli$tooltip;

    // TODO: MC 26.1 restructured getTooltipLines() (formerly getTooltip()). The MutableComponent.append
    // call no longer exists at the same point. Needs reimplementing against addDetailsToTooltip().
    // @Inject(method = "getTooltipLines", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/chat/MutableComponent;append(Lnet/minecraft/network/chat/Component;)Lnet/minecraft/network/chat/MutableComponent;"))
    // private void apoli$cacheTooltipStuff(Item.TooltipContext context, @Nullable Player player, TooltipFlag type, CallbackInfoReturnable<List<Component>> cir, @Local List<Component> tooltip) { ... }

    @Inject(method = "getTooltipLines", at = @At(value = "RETURN"))
    private void apoli$clearCachedTooltipStuff(CallbackInfoReturnable<?> cir) {
        this.apoli$appendedSlots = null;
        this.apoli$tooltipContext = null;
        this.apoli$tooltipType = null;
        this.apoli$tooltip = null;
    }

    // TODO: MC 26.1 removed Item.appendTooltip() from getTooltipLines(). The tooltip system
    // was restructured to use addDetailsToTooltip(). This needs reimplementing.
    // @Inject(method = "getTooltipLines", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/Item;appendTooltip(...)V", shift = At.Shift.AFTER))
    // private void apoli$appendUnusableTooltip(...) { ... }

    // TODO: MC 26.1 removed ItemStack.appendTooltip(). The tooltip system now uses
    // addDetailsToTooltip() with a different signature. Power tooltips need reimplementing.
    // @WrapOperation(method = "getTooltipLines", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;appendTooltip(Lnet/minecraft/core/component/DataComponentType;Lnet/minecraft/world/item/Item$TooltipContext;Ljava/util/function/Consumer;Lnet/minecraft/world/item/TooltipFlag;)V"))
    // private void apoli$appendPowerTooltips(ItemStack stack, DataComponentType<?> componentType, Item.TooltipContext context, Consumer<Component> tooltipConsumer, TooltipFlag type, Operation<Void> original, Item.TooltipContext mContext, @Nullable Player player, @Local List<Component> tooltip) {
    //     original.call(stack, componentType, context, tooltipConsumer, type);
    //     if (componentType == DataComponents.LORE) {
    //         PowerHolderComponent.getPowerTypes(player, TooltipPowerType.class)
    //             .stream()
    //             .filter(p -> p.doesApply((ItemStack) (Object) this))
    //             .sorted(Comparator.comparing(TooltipPowerType::getOrder))
    //             .forEach(p -> p.processTooltips(tooltipConsumer));
    //     }
    // }

    // TODO: MC 26.1 changed addAttributeTooltips signature and removed applyAttributeModifier.
    // The attribute tooltip system was restructured. Needs reimplementing.
    // @Inject(method = "addAttributeTooltips", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;applyAttributeModifier(...)V", shift = At.Shift.AFTER))
    // private void apoli$appendItemPowersTooltips(...) { ... }

    @Unique
    private void apoli$appendExpandedTooltip(List<PreventItemUsePowerType> powers, List<Component> tooltip, String translationKey, ChatFormatting powerTextColor, ChatFormatting baseTextColor) {

        List<Component> powerTexts = new LinkedList<>();
        for (PreventItemUsePowerType power : powers) {

            MutableComponent prependedText = Component.literal("  - ").withStyle(baseTextColor);
            MutableComponent powerText = power.getPower().getName().withStyle(powerTextColor);

            powerTexts.add(prependedText.append(powerText));

        }

        Component powerText = Component.translatable("tooltip.apoli.usability_hint.power_count", powers.size()).withStyle(powerTextColor);
        Component baseText = Component.translatable(translationKey, powerText).withStyle(baseTextColor);

        tooltip.add(baseText);
        tooltip.addAll(powerTexts);

    }

}
