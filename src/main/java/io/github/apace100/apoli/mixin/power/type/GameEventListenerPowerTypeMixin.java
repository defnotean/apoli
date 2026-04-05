package io.github.apace100.apoli.mixin.power.type;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.type.GameEventListenerPowerType;
import net.minecraft.world.entity.Entity;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.Holder;
import net.minecraft.tags.TagKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BiConsumer;

public abstract class GameEventListenerPowerTypeMixin {

	@Mixin(VibrationSystem.User.class)
	public interface CustomCallbackHandler {

		@WrapOperation(method = "canAccept", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/Holder;is(Lnet/minecraft/tags/TagKey;)Z", ordinal = 0))
		private boolean apoli$acceptsGameEvent(Holder<GameEvent> gameEvent, TagKey<GameEvent> gameEventTag, Operation<Boolean> original) {

			if ((VibrationSystem.User) this instanceof GameEventListenerPowerType.Callback powerCallback) {
				return powerCallback.containsEvent(gameEvent);
			}

			else {
				return original.call(gameEvent, gameEventTag);
			}

		}

	}

	@Mixin(VibrationSystem.Ticker.class)
	public interface ParticleAppearanceHandler {

		@WrapWithCondition(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;spawnParticles(Lnet/minecraft/core/particles/ParticleOptions;DDDIDDDD)I"))
		private static boolean apoli$onlyShowParticleWhenSpecified(ServerLevel world, ParticleOptions particle, double x, double y, double z, int count, double deltaX, double deltaY, double deltaZ, double speed, VibrationSystem.Data listenerData) {

			if (listenerData instanceof GameEventListenerPowerType.ListenerData powerListenerData) {
				return powerListenerData.shouldShowParticle();
			}

			else {
				return true;
			}

		}

	}

	@Mixin(Entity.class)
	public static abstract class EventHandlerUpdater {

		// MC 26.1: Entity.getWorld() no longer exists; level() is the direct method on Entity
		@Inject(method = "updateDynamicGameEventListener", at = @At("HEAD"))
		private void apoli$update(BiConsumer<VibrationSystem.Listener, ServerLevel> callback, CallbackInfo ci) {

			if (((Entity) (Object) this).level() instanceof ServerLevel serverWorld) {
				PowerHolderComponent.getPowerTypes((Entity) (Object) this, GameEventListenerPowerType.class, true)
					.stream()
					.map(GameEventListenerPowerType::getGameEventHandler)
					.forEach(listener -> callback.accept(listener.getListener(), serverWorld));
			}

		}

	}

}
