package io.github.apace100.apoli.action.type.bientity.meta;

import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.BiEntityAction;
import io.github.apace100.apoli.action.context.BiEntityActionContext;
import io.github.apace100.apoli.action.type.BiEntityActionType;
import io.github.apace100.apoli.action.type.BiEntityActionTypes;
import io.github.apace100.apoli.action.type.meta.ChoiceMetaActionType;
import net.minecraft.world.entity.ai.behavior.ShufflingList;
import org.jetbrains.annotations.NotNull;

public class ChoiceBiEntityActionType extends BiEntityActionType implements ChoiceMetaActionType<BiEntityActionContext, BiEntityAction> {

	private final ShufflingList<BiEntityAction> actions;

	public ChoiceBiEntityActionType(ShufflingList<BiEntityAction> actions) {
		this.actions = actions;
	}

	@Override
	public void accept(BiEntityActionContext context) {
		this.executeActions(context);
	}

	@Override
	public @NotNull ActionConfiguration<?> getConfig() {
		return BiEntityActionTypes.CHOICE;
	}

	@Override
	public ShufflingList<BiEntityAction> actions() {
		return actions;
	}

}
