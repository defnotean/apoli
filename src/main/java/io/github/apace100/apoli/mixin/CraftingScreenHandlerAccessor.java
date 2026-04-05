package io.github.apace100.apoli.mixin;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CraftingMenu.class)
public interface CraftingScreenHandlerAccessor {

    @Accessor
    Player getPlayer();

    // MC 26.1: field renamed from 'context' to 'access'
    @Accessor("access")
    ContainerLevelAccess getContext();
}
