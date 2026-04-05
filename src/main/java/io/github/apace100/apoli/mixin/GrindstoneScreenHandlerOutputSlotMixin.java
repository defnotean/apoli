package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.apace100.apoli.access.PowerModifiedGrindstone;
import io.github.apace100.apoli.power.type.ModifyGrindstonePowerType;
import io.github.apace100.apoli.util.modifier.Modifier;
import io.github.apace100.apoli.util.modifier.ModifierUtil;
import net.minecraft.world.inventory.GrindstoneMenu;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;
import java.util.Optional;

@Mixin(targets = "net/minecraft/world/inventory/GrindstoneMenu$4")
public abstract class GrindstoneScreenHandlerOutputSlotMixin {

    @Final
    @Shadow
    GrindstoneMenu this$0;

    // MC 26.1: method renamed from 'getExperience' to 'getExperienceAmount'
    @ModifyReturnValue(method = "getExperienceAmount(Lnet/minecraft/world/level/Level;)I", at = @At("RETURN"))
    private int apoli$modifyExperience(int original, Level world) {

        if (!(this$0 instanceof PowerModifiedGrindstone powerModifiedGrindstone)) {
            return original;
        }

        List<Modifier> modifiers = powerModifiedGrindstone.apoli$getAppliedPowers()
            .stream()
            .map(ModifyGrindstonePowerType::getExperienceModifier)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .toList();

        return (int) ModifierUtil.applyModifiers(powerModifiedGrindstone.apoli$getPlayer(), modifiers, original);

    }

}
