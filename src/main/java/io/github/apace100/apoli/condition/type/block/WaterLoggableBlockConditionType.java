package io.github.apace100.apoli.condition.type.block;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.context.BlockConditionContext;
import io.github.apace100.apoli.condition.type.BlockConditionType;
import io.github.apace100.apoli.condition.type.BlockConditionTypes;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import org.jetbrains.annotations.NotNull;

public class WaterLoggableBlockConditionType extends BlockConditionType {

	@Override
	public boolean test(BlockConditionContext context) {
		return context.blockState().getBlock() instanceof SimpleWaterloggedBlock;
	}

	@Override
	public @NotNull ConditionConfiguration<?> getConfig() {
		return BlockConditionTypes.WATER_LOGGABLE;
	}

}
