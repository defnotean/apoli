package io.github.apace100.apoli.action.type.block.meta;

import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.BlockAction;
import io.github.apace100.apoli.action.context.BlockActionContext;
import io.github.apace100.apoli.action.type.BlockActionType;
import io.github.apace100.apoli.action.type.BlockActionTypes;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.util.MiscUtil;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.core.Vec3i;
import org.jetbrains.annotations.NotNull;

public class OffsetBlockActionType extends BlockActionType {

    public static final TypedDataObjectFactory<OffsetBlockActionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("action", BlockAction.DATA_TYPE, null)
            .addFunctionedDefault("block_action", BlockAction.DATA_TYPE, data -> data.get("action"))
            .add("x", SerializableDataTypes.INT, 0)
            .add("y", SerializableDataTypes.INT, 0)
            .add("z", SerializableDataTypes.INT, 0)
            .addFunctionedDefault("offset", ApoliDataTypes.VECTOR_3_INT, data -> new Vec3i(data.get("x"), data.get("y"), data.get("z")))
            .validate(MiscUtil.validateAnyFieldsPresent("action", "block_action")),
        data -> new OffsetBlockActionType(
            data.get("block_action"),
            data.get("offset")
        ),
        (actionType, serializableData) -> serializableData.instance()
            .set("block_action", actionType.blockAction)
            .set("offset", actionType.offset)
    );

    private final BlockAction blockAction;
    private final Vec3i offset;

    public OffsetBlockActionType(BlockAction blockAction, Vec3i offset) {
        this.blockAction = blockAction;
        this.offset = offset;
    }

    @Override
    public void accept(BlockActionContext context) {
        blockAction.execute(context.world(), context.pos().add(offset), context.direction());
    }

    @Override
    public @NotNull ActionConfiguration<?> getConfig() {
        return BlockActionTypes.OFFSET;
    }

}
