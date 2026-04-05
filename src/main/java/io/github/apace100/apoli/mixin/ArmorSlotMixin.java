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

    // MC 26.1: field renamed from 'entity' to 'owner'
    @Shadow @Final private LivingEntity owner;

    // MC 26.1: field renamed from 'equipmentSlot' to 'slot'
    @Shadow @Final private EquipmentSlot slot;

    @ModifyReturnValue(method = "mayPlace", at = @At("RETURN"))
    private boolean apoli$preventArmorInsertion(boolean original, ItemStack stack) {
        return original
            && !PowerHolderComponent.hasPowerType(this.owner, RestrictArmorPowerType.class, p -> p.doesRestrict(stack, this.slot));
    }

}
