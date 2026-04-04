package io.github.apace100.apoli.action.type.block;

import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.context.BlockActionContext;
import io.github.apace100.apoli.action.type.BlockActionType;
import io.github.apace100.apoli.action.type.BlockActionTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class AddBlockBlockActionType extends BlockActionType {

    public static final TypedDataObjectFactory<AddBlockBlockActionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("block", SerializableDataTypes.BLOCK_STATE),
        data -> new AddBlockBlockActionType(
            data.get("block")
        ),
        (actionType, serializableData) -> serializableData.instance()
            .set("block", actionType.blockState)
    );

    private final BlockState blockState;

    public AddBlockBlockActionType(BlockState blockState) {
        this.blockState = blockState;
    }

    @Override
    public void accept(BlockActionContext context) {
        context.direction()
            .map(context.pos()::offset)
            .ifPresent(offsetPos -> context.world().setBlock(offsetPos, blockState));
    }

    @Override
    public @NotNull ActionConfiguration<?> getConfig() {
        return BlockActionTypes.ADD_BLOCK;
    }

}
