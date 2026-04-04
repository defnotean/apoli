package io.github.apace100.apoli.action.type.item.meta;

import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.ItemAction;
import io.github.apace100.apoli.action.context.ItemActionContext;
import io.github.apace100.apoli.action.type.ItemActionType;
import io.github.apace100.apoli.action.type.ItemActionTypes;
import io.github.apace100.apoli.action.type.meta.ChoiceMetaActionType;
import net.minecraft.world.entity.ai.behavior.ShufflingList;
import org.jetbrains.annotations.NotNull;

public class ChoiceItemActionType extends ItemActionType implements ChoiceMetaActionType<ItemActionContext, ItemAction> {

	private final ShufflingList<ItemAction> actions;

	public ChoiceItemActionType(ShufflingList<ItemAction> actions) {
		this.actions = actions;
	}

	@Override
	public void accept(ItemActionContext context) {
		this.executeActions(context);
	}

	@Override
	public @NotNull ActionConfiguration<?> getConfig() {
		return ItemActionTypes.CHOICE;
	}

	@Override
	public ShufflingList<ItemAction> actions() {
		return actions;
	}

}
