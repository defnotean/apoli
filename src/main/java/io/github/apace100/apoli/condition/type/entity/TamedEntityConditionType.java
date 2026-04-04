package io.github.apace100.apoli.condition.type.entity;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.context.EntityConditionContext;
import io.github.apace100.apoli.condition.type.EntityConditionType;
import io.github.apace100.apoli.condition.type.EntityConditionTypes;
import net.minecraft.world.entity.Tameable;
import org.jetbrains.annotations.NotNull;

public class TamedEntityConditionType extends EntityConditionType {

	@Override
	public boolean test(EntityConditionContext context) {
		return context.entity() instanceof Tameable tameable
			&& tameable.getOwnerUuid() != null;
	}

	@Override
	public @NotNull ConditionConfiguration<?> getConfig() {
		return EntityConditionTypes.TAMED;
	}

}
