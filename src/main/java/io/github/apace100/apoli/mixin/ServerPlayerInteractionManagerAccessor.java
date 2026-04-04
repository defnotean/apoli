package io.github.apace100.apoli.mixin;

import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.world.GameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerPlayerGameMode.class)
public interface ServerPlayerInteractionManagerAccessor {

    @Accessor
    BlockPos getMiningPos();

    @Accessor
    boolean getMining();

    @Accessor
    GameMode getGameMode();
}
