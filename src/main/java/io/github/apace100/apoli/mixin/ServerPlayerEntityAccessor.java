package io.github.apace100.apoli.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Optional;

@Mixin(ServerPlayer.class)
public interface ServerPlayerEntityAccessor {

    // MC 26.1: findRespawnPosition renamed and signature changed.
    // RespawnPosAngle is private so we return Optional<Object> to avoid access issues.
    // The actual return type at bytecode level is Optional<ServerPlayer.RespawnPosAngle>.

}
