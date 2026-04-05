package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.type.ModifyInsomniaTicksPowerType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.PhantomSpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PhantomSpawner.class)
public class PhantomSpawnerMixin {

    @ModifyExpressionValue(method = "spawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/stats/ServerStatsCounter;getStat(Lnet/minecraft/stats/Stat;)I"))
    private int apoli$modifyEffectiveTimeSinceRestValue(int original, ServerLevel world, boolean spawnMonsters, boolean spawnAnimals, @Local ServerPlayer player) {
        return (int) PowerHolderComponent.modify(player, ModifyInsomniaTicksPowerType.class, original);
    }

}
