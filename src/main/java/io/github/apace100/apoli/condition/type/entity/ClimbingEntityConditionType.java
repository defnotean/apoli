package io.github.apace100.apoli.condition.type.entity;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.context.EntityConditionContext;
import io.github.apace100.apoli.condition.type.EntityConditionType;
import io.github.apace100.apoli.condition.type.EntityConditionTypes;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

public class ClimbingEntityConditionType extends EntityConditionType {

	@Override
	public boolean test(EntityConditionContext context) {
		return context.entity() instanceof LivingEntity livingEntity
			&& livingEntity.onClimbable();
	}

	@Override
	public @NotNull ConditionConfiguration<?> getConfig() {
		return EntityConditionTypes.CLIMBING;
	}

}
