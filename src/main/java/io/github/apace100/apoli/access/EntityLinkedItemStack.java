package io.github.apace100.apoli.access;

import net.minecraft.world.entity.Entity;

public interface EntityLinkedItemStack {
    Entity apoli$getEntity();

    Entity apoli$getEntity(boolean prioritiseVanillaHolder);

    void apoli$setEntity(Entity entity);
}
