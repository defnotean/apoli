package io.github.apace100.apoli.condition.context;

import io.github.apace100.apoli.util.context.ConditionContext;
import net.minecraft.world.entity.Entity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public record EntityConditionContext(Entity entity) implements ConditionContext {

	public Level world() {
		return entity().level();
	}

	public BlockPos blockPos() {
		return entity().blockPosition();
	}

}
