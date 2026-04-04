package io.github.apace100.apoli.condition.type.biome;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.context.BiomeConditionContext;
import io.github.apace100.apoli.condition.type.BiomeConditionType;
import io.github.apace100.apoli.condition.type.BiomeConditionTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import org.jetbrains.annotations.NotNull;

public class InTagBiomeConditionType extends BiomeConditionType {

    public static final TypedDataObjectFactory<InTagBiomeConditionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("tag", SerializableDataTypes.BIOME_TAG),
        data -> new InTagBiomeConditionType(
            data.get("tag")
        ),
        (conditionType, serializableData) -> serializableData.instance()
            .set("tag", conditionType.tag)
    );

    private final TagKey<Biome> tag;

    public InTagBiomeConditionType(TagKey<Biome> tag) {
        this.tag = tag;
    }

    @Override
    public boolean test(BiomeConditionContext context) {
        return context.biomeEntry().is(tag);
    }

    @Override
    public @NotNull ConditionConfiguration<?> getConfig() {
        return BiomeConditionTypes.IN_TAG;
    }

}
