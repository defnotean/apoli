package io.github.apace100.apoli.condition.type.entity;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.context.EntityConditionContext;
import io.github.apace100.apoli.condition.type.EntityConditionType;
import io.github.apace100.apoli.condition.type.EntityConditionTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.util.MiscUtil;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class HasCommandTagEntityConditionType extends EntityConditionType {

    public static final TypedDataObjectFactory<HasCommandTagEntityConditionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("command_tag", SerializableDataTypes.STRING, null)
            .addFunctionedDefault("command_tags", SerializableDataTypes.STRINGS, data -> MiscUtil.singletonListOrEmpty(data.get("command_tag"))),
        data -> new HasCommandTagEntityConditionType(
            data.get("command_tags")
        ),
        (conditionType, serializableData) -> serializableData.instance()
            .set("command_tags", conditionType.commandTags)
    );

    private final List<String> commandTags;

    public HasCommandTagEntityConditionType(List<String> commandTags) {
        this.commandTags = commandTags;
    }

    @Override
    public boolean test(EntityConditionContext context) {

        Entity entity = context.entity();
        Set<String> entityCommandTags = entity.entityTags();

        return this.commandTags.isEmpty()
            ? !entityCommandTags.isEmpty()
            : !Collections.disjoint(entityCommandTags, this.commandTags);

    }

    @Override
    public @NotNull ConditionConfiguration<?> getConfig() {
        return EntityConditionTypes.HAS_COMMAND_TAG;
    }

}
