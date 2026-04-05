package io.github.apace100.apoli.mixin;

import com.google.common.collect.BiMap;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LootContextParamSets.class)
public interface LootContextTypesAccessor {

    @Accessor("REGISTRY")
    static BiMap<Identifier, ContextKeySet> getMap() {
        throw new AssertionError();
    }

}
