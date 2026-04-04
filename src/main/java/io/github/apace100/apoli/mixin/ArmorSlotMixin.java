package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.type.RestrictArmorPowerType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.ArmorSlot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ArmorSlot.class)
public abstract class ArmorSlotMixin {

    @Shadow @Final private LivingEntity entity;

    @Shadow @Final private EquipmentSlot equipmentSlot;

    @ModifyReturnValue(method = "canInsert", at = @At("RETURN"))
    private boolean apoli$preventArmorInsertion(boolean original, ItemStack stack) {
        return original
            && !PowerHolderComponent.hasPowerType(this.entity, RestrictArmorPowerType.class, p -> p.doesRestrict(stack, this.equipmentSlot));
    }

}
