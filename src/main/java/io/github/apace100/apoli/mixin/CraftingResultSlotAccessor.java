package io.github.apace100.apoli.mixin;

import net.minecraft.world.RecipeInputInventory;
import net.minecraft.world.inventory.CraftingResultSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CraftingResultSlot.class)
public interface CraftingResultSlotAccessor {
    @Accessor
    RecipeInputInventory getInput();
}
