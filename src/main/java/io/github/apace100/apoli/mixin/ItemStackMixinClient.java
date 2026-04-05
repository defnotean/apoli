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

    @Inject(method = "getTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/chat/MutableComponent;append(Lnet/minecraft/network/chat/Component;)Lnet/minecraft/network/chat/MutableComponent;"))
    private void apoli$cacheTooltipStuff(Item.TooltipContext context, @Nullable Player player, TooltipFlag type, CallbackInfoReturnable<List<Component>> cir, @Local List<Component> tooltip) {

		// Although this is a client-only mixin, this is still seen by the internal server.
        if (player == null || !player.level().isClientSide) {
            return;
        }

        this.apoli$appendedSlots = EnumSet.noneOf(EquipmentSlotGroup.class);
        this.apoli$tooltipContext = context;
        this.apoli$tooltipType = type;
        this.apoli$tooltip = tooltip;

    }

    @Inject(method = "getTooltip", at = @At(value = "RETURN"))
    private void apoli$clearCachedTooltipStuff(CallbackInfoReturnable<?> cir) {
        this.apoli$appendedSlots = null;
        this.apoli$tooltipContext = null;
        this.apoli$tooltipType = null;
        this.apoli$tooltip = null;
    }

    @Inject(method = "getTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/Item;appendTooltip(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/Item$TooltipContext;Ljava/util/List;Lnet/minecraft/world/item/tooltip/TooltipFlag;)V", shift = At.Shift.AFTER))
    private void apoli$appendUnusableTooltip(Item.TooltipContext context, @Nullable Player player, TooltipFlag type, CallbackInfoReturnable<List<Component>> cir) {

        if (!(Apoli.config instanceof ApoliConfigClient config) || !config.tooltips.showUsabilityHints) {
            return;
        }

        List<PreventItemUsePowerType> preventItemUsePowers = PowerHolderComponent.getPowerTypes(player, PreventItemUsePowerType.class)
            .stream()
            .filter(p -> p.doesPrevent((ItemStack) (Object) this))
            .toList();

        if (preventItemUsePowers.isEmpty()) {
            return;
        }

        String translationKey = "tooltip.apoli.unusable." + this.getUseAnimation().toString().toLowerCase(Locale.ROOT) + (preventItemUsePowers.size() == 1 ? ".single" : ".multiple");

        ChatFormatting baseTextFormat = ChatFormatting.GRAY;
        ChatFormatting powerTextFormat = ChatFormatting.RED;

        Component powerText;
        Component baseText;

        if (preventItemUsePowers.size() == 1) {

            PreventItemUsePowerType preventItemUsePower = preventItemUsePowers.getFirst();

            powerText = preventItemUsePower.getPower().getName().withStyle(powerTextFormat);
            baseText = Component.translatable(translationKey, powerText).withStyle(baseTextFormat);

            apoli$tooltip.add(baseText);

        }

        else if (config.tooltips.compactUsabilityHints) {

            Minecraft client = Minecraft.getInstance();
            KeyMapping keyBinding = ApoliClient.showPowersOnUsabilityHint;

            Integer keyCode = !keyBinding.isUnbound()
                ? InputConstants.fromTranslationKey(keyBinding.getBoundKeyTranslationKey()).getCode()
                : null;
            boolean isKeyPressed = keyCode != null
                && InputConstants.isKeyPressed(client.getWindow().getHandle(), keyCode);

            if (isKeyPressed) {
                this.apoli$appendExpandedTooltip(preventItemUsePowers, apoli$tooltip, translationKey, powerTextFormat, powerTextFormat);
            }

            else {

                powerText = Component.translatable("tooltip.apoli.usability_hint.power_count", preventItemUsePowers.size()).withStyle(powerTextFormat);
                baseText = Component.translatable(translationKey, powerText).withStyle(baseTextFormat);

                apoli$tooltip.add(baseText);
                apoli$tooltip.add(Component.empty());

                Component keyBindingText = KeyBindingUtil.getLocalizedName(keyBinding.getTranslationKey()).styled(style -> style
                    .withColor(ChatFormatting.YELLOW)
                    .withItalic(keyBinding.isUnbound()));

                Component guideText = Component.translatable("tooltip.apoli.usability_hint.show_powers", keyBindingText).withStyle(baseTextFormat);
                apoli$tooltip.add(guideText);

            }

        }

        else {
            this.apoli$appendExpandedTooltip(preventItemUsePowers, apoli$tooltip, translationKey, powerTextFormat, baseTextFormat);
        }

    }

    @WrapOperation(method = "getTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;appendTooltip(Lnet/minecraft/core/component/DataComponentType;Lnet/minecraft/world/item/Item$TooltipContext;Ljava/util/function/Consumer;Lnet/minecraft/world/item/tooltip/TooltipFlag;)V"))
    private void apoli$appendPowerTooltips(ItemStack stack, DataComponentType<?> componentType, Item.TooltipContext context, Consumer<Component> tooltipConsumer, TooltipFlag type, Operation<Void> original, Item.TooltipContext mContext, @Nullable Player player, @Local List<Component> tooltip) {

        original.call(stack, componentType, context, tooltipConsumer, type);

        if (componentType == DataComponents.LORE) {
            PowerHolderComponent.getPowerTypes(player, TooltipPowerType.class)
                .stream()
                .filter(p -> p.doesApply((ItemStack) (Object) this))
                .sorted(Comparator.comparing(TooltipPowerType::getOrder))
                .forEach(p -> p.processTooltips(tooltipConsumer));
        }

    }

    @Inject(method = "addAttributeTooltips", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;applyAttributeModifier(Lnet/minecraft/world/item/component/EquipmentSlotGroup;Ljava/util/function/BiConsumer;)V", shift = At.Shift.AFTER))
    private void apoli$appendItemPowersTooltips(Consumer<Component> tooltipConsumer, @Nullable Player player, CallbackInfo ci, @Local EquipmentSlotGroup modifierSlot, @Local MutableBoolean shouldAppendSlotName) {

        ItemPowersComponent itemPowersComponent = this.getOrDefault(ApoliDataComponentTypes.POWERS, ItemPowersComponent.DEFAULT);
        if (apoli$appendedSlots == null || apoli$appendedSlots.contains(modifierSlot) || !itemPowersComponent.containsSlot(modifierSlot)) {
            return;
        }

        if (shouldAppendSlotName.isTrue()) {

            tooltipConsumer.accept(CommonComponents.EMPTY);
            tooltipConsumer.accept(Component.translatable("item.modifiers." + modifierSlot.getSerializedName()).withStyle(ChatFormatting.GRAY));

            shouldAppendSlotName.setFalse();

        }

        itemPowersComponent.appendTooltip(modifierSlot, apoli$tooltipContext, apoli$tooltip::add, apoli$tooltipType);
        apoli$appendedSlots.add(modifierSlot);

    }

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
