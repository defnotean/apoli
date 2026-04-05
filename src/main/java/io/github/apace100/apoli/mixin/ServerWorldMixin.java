package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.type.PreventGameEventPowerType;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerLevel.class)
public class ServerWorldMixin {

    @WrapWithCondition(method = "gameEvent", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/gameevent/GameEventDispatcher;dispatch(Lnet/minecraft/core/Holder;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/level/gameevent/GameEvent$Emitter;)V"))
    private boolean apoli$prevenGameEvent(GameEventDispatcher manager, Holder<GameEvent> event, Vec3 emitterPos, GameEvent.Context emitter) {
        return emitter.sourceEntity() == null
            || !PowerHolderComponent.withPowerTypes(emitter.sourceEntity(), PreventGameEventPowerType.class, p -> p.doesPrevent(event), PreventGameEventPowerType::executeAction);
    }

}
