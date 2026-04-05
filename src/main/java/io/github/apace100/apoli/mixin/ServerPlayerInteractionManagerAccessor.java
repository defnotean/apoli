package io.github.apace100.apoli.mixin;

import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.GameType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerPlayerGameMode.class)
public interface ServerPlayerInteractionManagerAccessor {

    // MC 26.1: field renamed from 'miningPos' to 'destroyPos'
    @Accessor("destroyPos")
    BlockPos getMiningPos();

    // MC 26.1: field renamed from 'mining' to 'isDestroyingBlock'
    @Accessor("isDestroyingBlock")
    boolean getMining();

    // MC 26.1: field renamed from 'gameMode' to 'gameModeForPlayer'
    @Accessor("gameModeForPlayer")
    GameType getGameMode();
}
