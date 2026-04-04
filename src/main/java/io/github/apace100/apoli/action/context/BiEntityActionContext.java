package io.github.apace100.apoli.action.context;

import io.github.apace100.apoli.condition.context.BiEntityConditionContext;
import io.github.apace100.apoli.util.context.ActionContext;
import net.minecraft.world.entity.Entity;

public record BiEntityActionContext(Entity actor, Entity target) implements ActionContext<BiEntityConditionContext> {

	@Override
	public BiEntityConditionContext forCondition() {
		return new BiEntityConditionContext(actor(), target());
	}

}
