package io.github.apace100.apoli.mixin;

import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.ResultSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ResultSlot.class)
public interface CraftingResultSlotAccessor {
    @Accessor
    CraftingContainer getInput();
}
