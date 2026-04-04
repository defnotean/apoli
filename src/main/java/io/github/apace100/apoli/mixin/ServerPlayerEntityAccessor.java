package io.github.apace100.apoli.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Optional;

@Mixin(ServerPlayer.class)
public interface ServerPlayerEntityAccessor {

    @Invoker
    static Optional<ServerPlayer.RespawnPos> callFindRespawnPosition(ServerLevel world, BlockPos pos, float spawnAngle, boolean spawnForced, boolean alive) {
        throw new AssertionError();
    }

}
