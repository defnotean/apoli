package io.github.apace100.apoli.condition.type.entity;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.context.EntityConditionContext;
import io.github.apace100.apoli.condition.type.EntityConditionType;
import io.github.apace100.apoli.condition.type.EntityConditionTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class DimensionEntityConditionType extends EntityConditionType {

    public static final TypedDataObjectFactory<DimensionEntityConditionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("dimension", SerializableDataTypes.DIMENSION),
        data -> new DimensionEntityConditionType(
            data.get("dimension")
        ),
        (conditionType, serializableData) -> serializableData.instance()
            .set("dimension", conditionType.dimension)
    );

    private final ResourceKey<Level> dimension;

    public DimensionEntityConditionType(ResourceKey<Level> dimension) {
        this.dimension = dimension;
    }

    @Override
    public boolean test(EntityConditionContext context) {
        return context.entity().level().dimension().equals(dimension);
    }

    @Override
    public @NotNull ConditionConfiguration<?> getConfig() {
        return EntityConditionTypes.DIMENSION;
    }

}
