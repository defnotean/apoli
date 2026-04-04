package io.github.apace100.apoli.action.context;

import io.github.apace100.apoli.condition.context.EntityConditionContext;
import io.github.apace100.apoli.util.context.ActionContext;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public record EntityActionContext(Entity entity, Vec3 offset) implements ActionContext<EntityConditionContext> {

	public EntityActionContext(Entity entity) {
		this(entity, Vec3.ZERO);
	}

	@Override
	public EntityConditionContext forCondition() {
		return new EntityConditionContext(entity());
	}

}
