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

    @WrapWithCondition(method = "detachLeash(Lnet/minecraft/world/entity/Entity;ZZ)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;spawnAtLocation(Lnet/minecraft/world/level/ItemLike;)Lnet/minecraft/world/entity/ItemEntity;"))
    private static boolean apoli$preventDroppingLeashOnCustom(Entity entity, ItemLike item) {
        return !(entity instanceof CustomLeashable customLeashable)
            || !customLeashable.apoli$isCustomLeashed();
    }

    @Inject(method = "detachLeash(Lnet/minecraft/world/entity/Entity;ZZ)V", at = @At("TAIL"))
    private static void apoli$resetCustomLeashStatus(Entity entity, boolean sendPacket, boolean dropItem, CallbackInfo ci) {

        if (entity instanceof CustomLeashable customLeashable) {
            customLeashable.apoli$setCustomLeashed(false);
        }

    }

}
