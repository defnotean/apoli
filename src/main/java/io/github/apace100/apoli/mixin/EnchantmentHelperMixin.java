package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.apace100.apoli.power.type.ModifyEnchantmentLevelPowerType;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.Holder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(EnchantmentHelper.class)
public class EnchantmentHelperMixin {

    @WrapOperation(method = "getLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/ItemEnchantments;getLevel(Lnet/minecraft/core/Holder;)I"))
    private static int apoli$modifyEnchantmentsOnLevelQuery(ItemEnchantments enchantmentsComponent, Holder<Enchantment> enchantment, Operation<Integer> original, Holder<Enchantment> mEnchantment, ItemStack stack) {
        return original.call(ModifyEnchantmentLevelPowerType.getAndUpdateModifiedEnchantments(stack, enchantmentsComponent), enchantment);
    }

    @ModifyVariable(method = "forEachEnchantment(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/enchantment/EnchantmentHelper$Consumer;)V", at = @At("STORE"))
    private static ItemEnchantments apoli$modifyEnchantmentsOnForEach(ItemEnchantments original, ItemStack stack) {
        return ModifyEnchantmentLevelPowerType.getAndUpdateModifiedEnchantments(stack, original);
    }

    @ModifyExpressionValue(method = "forEachEnchantment(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/EquipmentSlot;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/enchantment/EnchantmentHelper$ContextAwareConsumer;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z"))
    private static boolean apoli$allowWorkableEmptiesInForEach(boolean original, ItemStack stack) {
        return original && !ModifyEnchantmentLevelPowerType.isWorkableEmptyStack(stack);
    }

    @ModifyVariable(method = "forEachEnchantment(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/EquipmentSlot;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/enchantment/EnchantmentHelper$ContextAwareConsumer;)V", at = @At("STORE"))
    private static ItemEnchantments apoli$modifyEnchantmentsOnForEachWithContext(ItemEnchantments original, ItemStack stack) {
        return ModifyEnchantmentLevelPowerType.getAndUpdateModifiedEnchantments(stack, original);
    }

    @ModifyVariable(method = "hasAnyEnchantmentsIn", at = @At("STORE"))
    private static ItemEnchantments apoli$modifyEnchantmentsOnInTagQuery(ItemEnchantments original, ItemStack stack) {
        return ModifyEnchantmentLevelPowerType.getAndUpdateModifiedEnchantments(stack, original);
    }

}
