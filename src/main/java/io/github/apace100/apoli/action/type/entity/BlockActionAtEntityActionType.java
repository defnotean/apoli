package io.github.apace100.apoli.action.type.entity;

import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.BlockAction;
import io.github.apace100.apoli.action.context.EntityActionContext;
import io.github.apace100.apoli.action.type.EntityActionType;
import io.github.apace100.apoli.action.type.EntityActionTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.world.entity.Entity;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class BlockActionAtEntityActionType extends EntityActionType {

    public static final TypedDataObjectFactory<BlockActionAtEntityActionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("block_action", BlockAction.DATA_TYPE),
        data -> new BlockActionAtEntityActionType(
            data.get("block_action")
        ),
        (actionType, serializableData) -> serializableData.instance()
            .set("block_action", actionType.blockAction)
    );

    private final BlockAction blockAction;

    public BlockActionAtEntityActionType(BlockAction blockAction) {
        this.blockAction = blockAction;
    }

    @Override
    public void accept(EntityActionContext context) {

        Entity entity = context.entity();
        BlockPos blockPos = BlockPos.ofFloored(entity.position().add(context.offset()));

        blockAction.execute(entity.level(), blockPos, Optional.empty());

    }

    @Override
    public @NotNull ActionConfiguration<?> getConfig() {
        return EntityActionTypes.BLOCK_ACTION_AT;
    }

}
