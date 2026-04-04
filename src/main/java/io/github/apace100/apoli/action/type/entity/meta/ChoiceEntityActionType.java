package io.github.apace100.apoli.action.type.entity.meta;

import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.EntityAction;
import io.github.apace100.apoli.action.context.EntityActionContext;
import io.github.apace100.apoli.action.type.EntityActionType;
import io.github.apace100.apoli.action.type.EntityActionTypes;
import io.github.apace100.apoli.action.type.meta.ChoiceMetaActionType;
import net.minecraft.world.entity.ai.behavior.ShufflingList;
import org.jetbrains.annotations.NotNull;

public class ChoiceEntityActionType extends EntityActionType implements ChoiceMetaActionType<EntityActionContext, EntityAction> {

	private final ShufflingList<EntityAction> actions;

	public ChoiceEntityActionType(ShufflingList<EntityAction> actions) {
		this.actions = actions;
	}

	@Override
	public void accept(EntityActionContext context) {
		this.executeActions(context);
	}

	@Override
	public boolean shouldExecute(EntityActionContext context) {
		return true;
	}

	@Override
	public @NotNull ActionConfiguration<?> getConfig() {
		return EntityActionTypes.CHOICE;
	}

	@Override
	public ShufflingList<EntityAction> actions() {
		return actions;
	}

}
