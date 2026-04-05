package io.github.apace100.apoli.mixin;

import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.ResultSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ResultSlot.class)
public interface CraftingResultSlotAccessor {
    // MC 26.1: field renamed from 'input' to 'craftSlots'
    @Accessor("craftSlots")
    CraftingContainer getInput();
}
