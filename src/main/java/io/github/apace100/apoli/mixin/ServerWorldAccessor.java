package io.github.apace100.apoli.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.entity.EntityLookup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ServerLevel.class)
public interface ServerWorldAccessor {

    @Invoker
    EntityLookup<Entity> callGetEntityLookup();

}
