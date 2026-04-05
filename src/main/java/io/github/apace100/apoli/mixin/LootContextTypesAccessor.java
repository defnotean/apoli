package io.github.apace100.apoli.mixin;

import com.google.common.collect.BiMap;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LootContextParamSets.class)
public interface LootContextTypesAccessor {

    @Accessor("MAP")
    static BiMap<Identifier, LootContextParamSets> getMap() {
        throw new AssertionError();
    }

}
