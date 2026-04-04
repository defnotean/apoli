package io.github.apace100.apoli.mixin;

import com.google.common.collect.BiMap;
import net.minecraft.world.level.storage.loot.LootContextParamSet;
import net.minecraft.world.level.storage.loot.BuiltInLootContextParamSets;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BuiltInLootContextParamSets.class)
public interface LootContextTypesAccessor {

    @Accessor("MAP")
    static BiMap<Identifier, LootContextParamSet> getMap() {
        throw new AssertionError();
    }

}
