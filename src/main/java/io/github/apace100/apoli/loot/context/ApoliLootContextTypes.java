package io.github.apace100.apoli.loot.context;

import com.google.common.collect.BiMap;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.mixin.LootContextTypesAccessor;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.resources.Identifier;

public class ApoliLootContextTypes {

    public static final ContextKeySet ANY = register(
        Apoli.identifier("any"),
        new ContextKeySet.Builder()
            .optional(LootContextParams.THIS_ENTITY)
            .optional(LootContextParams.LAST_DAMAGE_PLAYER)
            .optional(LootContextParams.DAMAGE_SOURCE)
            .optional(LootContextParams.ATTACKING_ENTITY)
            .optional(LootContextParams.DIRECT_ATTACKING_ENTITY)
            .optional(LootContextParams.ORIGIN)
            .optional(LootContextParams.BLOCK_STATE)
            .optional(LootContextParams.BLOCK_ENTITY)
            .optional(LootContextParams.TOOL)
            .optional(LootContextParams.EXPLOSION_RADIUS)
            .build()
    );

    private ApoliLootContextTypes() {}

    private static ContextKeySet register(Identifier id, ContextKeySet lootContextType) {

        BiMap<Identifier, ContextKeySet> idAndLootContextTypeMap = LootContextTypesAccessor.getMap();

        if (idAndLootContextTypeMap.containsKey(id)) {
            throw new IllegalStateException("Loot table parameter set \"" + id + "\" is already registered!");
        }

        idAndLootContextTypeMap.put(id, lootContextType);
        return lootContextType;

    }

}
