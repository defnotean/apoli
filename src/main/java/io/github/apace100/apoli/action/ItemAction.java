package io.github.apace100.apoli.action;

import io.github.apace100.apoli.action.context.ItemActionContext;
import io.github.apace100.apoli.action.type.ItemActionType;
import io.github.apace100.apoli.action.type.ItemActionTypes;
import io.github.apace100.apoli.action.type.item.meta.SequenceItemActionType;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.type.ModifyEnchantmentLevelPowerType;
import io.github.apace100.calio.data.SerializableDataType;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

public final class ItemAction extends Action<ItemActionContext, ItemActionType> {

	public static final SerializableDataType<ItemAction> DATA_TYPE = SerializableDataType.lazy(() -> ApoliDataTypes.actions("type", ItemActionTypes.DATA_TYPE, SequenceItemActionType::new, ItemAction::new));

	public ItemAction(ItemActionType actionType) {
		super(actionType);
	}

	public void execute(Level world, SlotAccess stackReference) {

		if (world instanceof ServerLevel serverWorld) {
			accept(new ItemActionContext(serverWorld, stackReference));
		}

	}

	@Override
	public void accept(ItemActionContext context) {

		SlotAccess stackReference = context.stackReference();

		//	Replace the stack of the stack reference with a "workable" empty stack if the said stack is an
		//	empty stack
		if (stackReference.get() == ItemStack.EMPTY) {
			stackReference.set(new ItemStack((Void) null));
		}

		//	Execute the action type
		getType().accept(context);

		//	Restore the empty stack instance of the stack reference afterward
		if (!ModifyEnchantmentLevelPowerType.isWorkableEmptyStack(stackReference) && stackReference.get().isEmpty()) {
			stackReference.set(ItemStack.EMPTY);
		}

	}

}
