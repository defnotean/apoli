package io.github.apace100.apoli.access;

import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.resources.ResourceKey;

public interface ReplacingLootContext extends LootContextTypeHolder {

    void apoli$setReplaced(ResourceKey<LootTable> key);

    boolean apoli$isReplaced(ResourceKey<LootTable> key);
}
