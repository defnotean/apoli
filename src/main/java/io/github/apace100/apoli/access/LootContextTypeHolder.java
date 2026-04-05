package io.github.apace100.apoli.access;

import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

public interface LootContextTypeHolder {

	LootContextParamSets apoli$getType();

	void apoli$setType(LootContextParamSets type);

}
