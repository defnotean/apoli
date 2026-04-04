package io.github.apace100.apoli.mixin;

import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.world.GameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MultiPlayerGameMode.class)
public interface ClientPlayerInteractionManagerAccessor {

    @Accessor
    BlockPos getCurrentBreakingPos();

    @Accessor
    boolean getBreakingBlock();

    @Accessor
    GameMode getGameMode();
}
