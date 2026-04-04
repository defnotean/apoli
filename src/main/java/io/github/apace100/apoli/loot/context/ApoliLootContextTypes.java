package io.github.apace100.apoli.loot.context;

import com.google.common.collect.BiMap;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.mixin.LootContextTypesAccessor;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.LootContextParamSet;
import net.minecraft.resources.ResourceLocation;

public class ApoliLootContextTypes {

    public static final LootContextParamSet ANY = register(
        Apoli.identifier("any"),
        LootContextParamSet.create()
            .allow(LootContextParams.THIS_ENTITY)
            .allow(LootContextParams.LAST_DAMAGE_PLAYER)
            .allow(LootContextParams.DAMAGE_SOURCE)
            .allow(LootContextParams.ATTACKING_ENTITY)
            .allow(LootContextParams.DIRECT_ATTACKING_ENTITY)
            .allow(LootContextParams.ORIGIN)
            .allow(LootContextParams.BLOCK_STATE)
            .allow(LootContextParams.BLOCK_ENTITY)
            .allow(LootContextParams.TOOL)
            .allow(LootContextParams.EXPLOSION_RADIUS)
    );

    private ApoliLootContextTypes() {}

    private static LootContextParamSet register(ResourceLocation id, LootContextParamSet.Builder lootContextTypeBuilder) {

        LootContextParamSet lootContextType = lootContextTypeBuilder.build();
        BiMap<ResourceLocation, LootContextParamSet> idAndLootContextTypeMap = LootContextTypesAccessor.getMap();

        if (idAndLootContextTypeMap.containsKey(id)) {
            throw new IllegalStateException("Loot table parameter set \"" + id + "\" is already registered!");
        }

        idAndLootContextTypeMap.put(id, lootContextType);
        return lootContextType;

    }

}
