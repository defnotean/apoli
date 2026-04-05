package io.github.apace100.apoli.condition.type.entity;

import io.github.apace100.apoli.condition.BlockCondition;
import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.context.EntityConditionContext;
import io.github.apace100.apoli.condition.type.EntityConditionType;
import io.github.apace100.apoli.condition.type.EntityConditionTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class OnBlockEntityConditionType extends EntityConditionType {

    public static final TypedDataObjectFactory<OnBlockEntityConditionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("block_condition", BlockCondition.DATA_TYPE.optional(), Optional.empty()),
        data -> new OnBlockEntityConditionType(
            data.get("block_condition")
        ),
        (conditionType, serializableData) -> serializableData.instance()
            .set("block_condition", conditionType.blockCondition)
    );

    private final Optional<BlockCondition> blockCondition;

    public OnBlockEntityConditionType(Optional<BlockCondition> blockCondition) {
        this.blockCondition = blockCondition;
    }

    @Override
    public boolean test(EntityConditionContext context) {
        Entity entity = context.entity();
        return entity.onGround()
            && blockCondition.map(condition -> condition.test(entity.level(), entity.getOnPos())).orElse(true);
    }

    @Override
    public @NotNull ConditionConfiguration<?> getConfig() {
        return EntityConditionTypes.ON_BLOCK;
    }

}
