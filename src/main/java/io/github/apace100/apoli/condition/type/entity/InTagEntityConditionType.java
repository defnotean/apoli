package io.github.apace100.apoli.condition.type.entity;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.context.EntityConditionContext;
import io.github.apace100.apoli.condition.type.EntityConditionType;
import io.github.apace100.apoli.condition.type.EntityConditionTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.tags.TagKey;
import org.jetbrains.annotations.NotNull;

public class InTagEntityConditionType extends EntityConditionType {

    public static final TypedDataObjectFactory<InTagEntityConditionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("tag", SerializableDataTypes.ENTITY_TAG),
        data -> new InTagEntityConditionType(
            data.get("tag")
        ),
        (conditionType, serializableData) -> serializableData.instance()
            .set("tag", conditionType.tag)
    );

    private final TagKey<EntityType<?>> tag;

    public InTagEntityConditionType(TagKey<EntityType<?>> tag) {
        this.tag = tag;
    }

    @Override
    public boolean test(EntityConditionContext context) {
        return context.entity().getType().is(tag);
    }

    @Override
    public @NotNull ConditionConfiguration<?> getConfig() {
        return EntityConditionTypes.IN_TAG;
    }

}
