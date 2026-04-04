package io.github.apace100.apoli.access;

import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.ReloadableRegistries;
import net.minecraft.core.registries.Registries;

public interface KeyableLootTable {

    ResourceKey<LootTable> apoli$getKey();

    void apoli$setup(ResourceKey<LootTable> lootTableKey, ReloadableRegistries.Lookup lookup);

}
