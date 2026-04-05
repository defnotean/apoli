package io.github.apace100.apoli.condition.type.entity;

import io.github.apace100.apoli.condition.BiomeCondition;
import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.context.EntityConditionContext;
import io.github.apace100.apoli.condition.type.EntityConditionType;
import io.github.apace100.apoli.condition.type.EntityConditionTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import net.minecraft.world.entity.Entity;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import net.minecraft.core.registries.Registries;

public class BiomeEntityConditionType extends EntityConditionType {

    public static final TypedDataObjectFactory<BiomeEntityConditionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("condition", BiomeCondition.DATA_TYPE.optional(), Optional.empty())
            .add("biome", SerializableDataType.registryKey(Registries.BIOME).optional(), Optional.empty())
            .add("biomes", SerializableDataType.registryKey(Registries.BIOME).list().optional(), Optional.empty()),
        data -> new BiomeEntityConditionType(
            data.get("condition"),
            data.get("biome"),
            data.get("biomes")
        ),
        (conditionType, serializableData) -> serializableData.instance()
            .set("condition", conditionType.biomeCondition)
            .set("biome", conditionType.biome)
            .set("biomes", conditionType.biomes)
    );

    private final Optional<BiomeCondition> biomeCondition;

    private final Optional<ResourceKey<Biome>> biome;
    private final Optional<List<ResourceKey<Biome>>> biomes;

    public BiomeEntityConditionType(Optional<BiomeCondition> biomeCondition, Optional<ResourceKey<Biome>> biome, Optional<List<ResourceKey<Biome>>> biomes) {
        this.biomeCondition = biomeCondition;
        this.biome = biome;
        this.biomes = biomes;
    }

    @Override
    public boolean test(EntityConditionContext context) {

        Entity entity = context.entity();

        Holder<Biome> biomeEntry = entity.level().getBiome(entity.getBlockPos());
        ResourceKey<Biome> biomeKey = biomeEntry.getKey().orElseThrow();

        return biome.map(biomeKey::equals).orElse(true)
            && biomes.map(keys -> keys.contains(biomeKey)).orElse(true)
            && biomeCondition.map(condition -> condition.test(entity.getBlockPos(), biomeEntry)).orElse(true);

    }

    @Override
    public @NotNull ConditionConfiguration<?> getConfig() {
        return EntityConditionTypes.BIOME;
    }

}
