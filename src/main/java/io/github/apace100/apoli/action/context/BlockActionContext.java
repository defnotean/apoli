package io.github.apace100.apoli.action.context;

import io.github.apace100.apoli.condition.context.BlockConditionContext;
import io.github.apace100.apoli.util.context.ActionContext;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.Optional;

public record BlockActionContext(ServerLevel world, BlockPos pos, Optional<Direction> direction) implements ActionContext<BlockConditionContext> {

	@Override
	public BlockConditionContext forCondition() {
		return new BlockConditionContext(world(), pos());
	}

}
