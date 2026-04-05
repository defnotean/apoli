package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.type.RestrictArmorPowerType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Equipment.class)
public interface EquipmentMixin {

    @ModifyExpressionValue(method = "handleEquipmentSwap", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;canUseSlot(Lnet/minecraft/world/entity/EquipmentSlot;)Z"))
    private boolean apoli$preventArmorEquipping(boolean original, Item item, Level world, Player user, @Local ItemStack stack, @Local EquipmentSlot slot) {
        return original
            && !PowerHolderComponent.hasPowerType(user, RestrictArmorPowerType.class, p -> p.doesRestrict(stack, slot));
    }

}
