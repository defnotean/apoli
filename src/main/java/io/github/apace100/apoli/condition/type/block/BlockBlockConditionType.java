package io.github.apace100.apoli.condition.type.block;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.context.BlockConditionContext;
import io.github.apace100.apoli.condition.type.BlockConditionType;
import io.github.apace100.apoli.condition.type.BlockConditionTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

public class BlockBlockConditionType extends BlockConditionType {

    public static final TypedDataObjectFactory<BlockBlockConditionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("block", SerializableDataTypes.BLOCK),
        data -> new BlockBlockConditionType(
            data.get("block")
        ),
        (conditionType, serializableData) -> serializableData.instance()
            .set("block", conditionType.block)
    );

    private final Block block;

    public BlockBlockConditionType(Block block) {
        this.block = block;
    }

    @Override
    public boolean test(BlockConditionContext context) {
        return context.blockState().is(block);
    }

    @Override
    public @NotNull ConditionConfiguration<?> getConfig() {
        return BlockConditionTypes.BLOCK;
    }

}
