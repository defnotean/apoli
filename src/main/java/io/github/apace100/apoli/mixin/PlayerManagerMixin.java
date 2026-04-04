package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.apace100.apoli.access.EndRespawningEntity;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.type.PowerType;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.players.PlayerList;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = PlayerList.class, priority = 800)
public abstract class PlayerManagerMixin {

	@WrapWithCondition(method = "respawnPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayer;setSpawnPointFrom(Lnet/minecraft/server/network/ServerPlayer;)V"))
	private boolean apoli$preventEndExitSpawnpointResetting(ServerPlayer newPlayer, ServerPlayer oldPlayer) {
		return ((EndRespawningEntity) oldPlayer).apoli$hasRealRespawnPoint();
	}

	@Inject(method = "respawnPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayer;onSpawn()V"))
	private void apoli$invokeOnRespawnPowerCallback(ServerPlayer player, boolean alive, Entity.RemovalReason removalReason, CallbackInfoReturnable<ServerPlayer> cir, @Local(ordinal = 1) ServerPlayer newPlayer) {
		if (!alive) {
			PowerHolderComponent.KEY.get(newPlayer).getPowerTypes().forEach(PowerType::onRespawn);
		}
	}

}
