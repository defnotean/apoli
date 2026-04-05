package io.github.apace100.apoli.condition.type.block;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.context.BlockConditionContext;
import io.github.apace100.apoli.condition.type.BlockConditionType;
import io.github.apace100.apoli.condition.type.BlockConditionTypes;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class MovementBlockingBlockConditionType extends BlockConditionType {

    @Override
    public boolean test(BlockConditionContext context) {
        BlockState blockState = context.blockState();
        return blockState.blocksMotion()
            && !blockState.getCollisionShape(context.world(), context.pos()).isEmpty();
    }

    @Override
    public @NotNull ConditionConfiguration<?> getConfig() {
        return BlockConditionTypes.MOVEMENT_BLOCKING;
    }

}
