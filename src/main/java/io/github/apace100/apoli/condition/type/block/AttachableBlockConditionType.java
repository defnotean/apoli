package io.github.apace100.apoli.condition.type.block;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.context.BlockConditionContext;
import io.github.apace100.apoli.condition.type.BlockConditionType;
import io.github.apace100.apoli.condition.type.BlockConditionTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class AttachableBlockConditionType extends BlockConditionType {

    @Override
    public boolean test(BlockConditionContext context) {

        Level world = context.world();
        BlockPos pos = context.pos();

        for (Direction direction : Direction.values()) {

            BlockPos offsetPos = pos.relative(direction);

            if (world.hasChunkAt(offsetPos) && world.getBlockState(offsetPos).isFaceSturdy(world, pos, direction.getOpposite())) {
                return true;
            }

        }

        return false;

    }

    @Override
    public @NotNull ConditionConfiguration<?> getConfig() {
        return BlockConditionTypes.ATTACHABLE;
    }

}
