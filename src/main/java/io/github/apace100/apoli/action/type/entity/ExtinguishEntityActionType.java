package io.github.apace100.apoli.action.type.entity;

import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.context.EntityActionContext;
import io.github.apace100.apoli.action.type.EntityActionType;
import io.github.apace100.apoli.action.type.EntityActionTypes;
import org.jetbrains.annotations.NotNull;

public class ExtinguishEntityActionType extends EntityActionType {

	@Override
	public void accept(EntityActionContext context) {
		context.entity().extinguishFire();
	}

	@Override
	public @NotNull ActionConfiguration<?> getConfig() {
		return EntityActionTypes.EXTINGUISH;
	}

}
