package io.github.apace100.apoli.mixin;

import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.GameType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

// MC 26.1: target was incorrectly GameType.class, should be MultiPlayerGameMode.class
@Mixin(MultiPlayerGameMode.class)
public interface ClientPlayerInteractionManagerAccessor {

    // MC 26.1: field renamed from 'currentBreakingPos' to 'destroyBlockPos'
    @Accessor("destroyBlockPos")
    BlockPos getCurrentBreakingPos();

    // MC 26.1: field renamed from 'breakingBlock' to 'isDestroying'
    @Accessor("isDestroying")
    boolean getBreakingBlock();

    @Accessor("localPlayerMode")
    GameType getGameMode();
}
