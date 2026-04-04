package io.github.apace100.apoli.action.type.block.meta;

import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.BlockAction;
import io.github.apace100.apoli.action.context.BlockActionContext;
import io.github.apace100.apoli.action.type.BlockActionType;
import io.github.apace100.apoli.action.type.BlockActionTypes;
import io.github.apace100.apoli.action.type.meta.ChoiceMetaActionType;
import net.minecraft.world.entity.ai.behavior.ShufflingList;
import org.jetbrains.annotations.NotNull;

public class ChoiceBlockActionType extends BlockActionType implements ChoiceMetaActionType<BlockActionContext, BlockAction> {

	private final ShufflingList<BlockAction> actions;

	public ChoiceBlockActionType(ShufflingList<BlockAction> actions) {
		this.actions = actions;
	}

	@Override
	public void accept(BlockActionContext context) {
		this.executeActions(context);
	}

	@Override
	public @NotNull ActionConfiguration<?> getConfig() {
		return BlockActionTypes.CHOICE;
	}

	@Override
	public ShufflingList<BlockAction> actions() {
		return actions;
	}

}
