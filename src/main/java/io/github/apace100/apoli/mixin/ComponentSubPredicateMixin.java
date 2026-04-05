package io.github.apace100.apoli.mixin;

// TODO: MC 26.1 - ItemSubPredicate removed. This mixin needs rework.
/*
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.apace100.apoli.power.type.ModifyEnchantmentLevelPowerType;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.ItemStack;
import net.minecraft.advancements.critereon.ItemSubPredicate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemSubPredicate.class)
public interface ComponentSubPredicateMixin {

    @WrapOperation(method = "test(Lnet/minecraft/world/item/ItemStack;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;get(Lnet/minecraft/core/component/DataComponentType;)Ljava/lang/Object;"))
    private Object apoli$accountForModifiedEnchantments(ItemStack stack, DataComponentType<?> componentType, Operation<Object> original) {
        ...
    }

}
*/

// Stub interface to prevent compilation errors
public interface ComponentSubPredicateMixin {
}
