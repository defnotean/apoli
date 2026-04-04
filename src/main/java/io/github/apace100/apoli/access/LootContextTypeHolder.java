package io.github.apace100.apoli.access;

import net.minecraft.world.level.storage.loot.LootContextParamSet;

public interface LootContextTypeHolder {

	LootContextParamSet apoli$getType();

	void apoli$setType(LootContextParamSet type);

}
