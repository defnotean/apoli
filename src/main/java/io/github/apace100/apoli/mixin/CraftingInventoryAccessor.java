package io.github.apace100.apoli.mixin;

import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(TransientCraftingContainer.class)
public interface CraftingInventoryAccessor {

    // MC 26.1: field renamed from 'handler' to 'menu'
    @Accessor("menu")
    AbstractContainerMenu getHandler();
}
