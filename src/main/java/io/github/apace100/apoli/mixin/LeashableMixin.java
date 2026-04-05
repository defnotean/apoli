package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import io.github.apace100.apoli.access.CustomLeashable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.level.ItemLike;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Leashable.class)
public interface LeashableMixin {

    // MC 26.1: Entity.spawnAtLocation now takes (ServerLevel, ItemLike) instead of just (ItemLike)
    @WrapWithCondition(method = "dropLeash(Lnet/minecraft/world/entity/Entity;ZZ)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;spawnAtLocation(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/ItemLike;)Lnet/minecraft/world/entity/item/ItemEntity;"))
    private static boolean apoli$preventDroppingLeashOnCustom(Entity entity, net.minecraft.server.level.ServerLevel serverLevel, ItemLike item) {
        return !(entity instanceof CustomLeashable customLeashable)
            || !customLeashable.apoli$isCustomLeashed();
    }

    @Inject(method = "dropLeash(Lnet/minecraft/world/entity/Entity;ZZ)V", at = @At("TAIL"))
    private static void apoli$resetCustomLeashStatus(Entity entity, boolean sendPacket, boolean dropItem, CallbackInfo ci) {

        if (entity instanceof CustomLeashable customLeashable) {
            customLeashable.apoli$setCustomLeashed(false);
        }

    }

}
