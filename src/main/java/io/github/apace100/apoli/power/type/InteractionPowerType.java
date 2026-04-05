package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.action.ItemAction;
import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.condition.ItemCondition;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.util.InventoryUtil;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;

import java.util.EnumSet;
import java.util.Optional;
import java.util.function.BiFunction;

public abstract class InteractionPowerType extends PowerType {

    protected final Optional<ItemAction> heldItemAction;
    protected final Optional<ItemCondition> heldItemCondition;

    protected final Optional<ItemAction> resultItemAction;
    protected final Optional<ItemStack> resultStack;

    protected final EnumSet<InteractionHand> hands;
    protected final InteractionResult actionResult;

    public InteractionPowerType(Optional<ItemAction> heldItemAction, Optional<ItemCondition> heldItemCondition, Optional<ItemAction> resultItemAction, Optional<ItemStack> resultStack, EnumSet<InteractionHand> hands, InteractionResult actionResult, Optional<EntityCondition> condition) {
        super(condition);
        this.hands = hands;
        this.actionResult = actionResult;
        this.heldItemCondition = heldItemCondition;
        this.heldItemAction = heldItemAction;
        this.resultStack = resultStack;
        this.resultItemAction = resultItemAction;
    }

    public InteractionPowerType(Optional<ItemAction> heldItemAction, Optional<ItemCondition> heldItemCondition, Optional<ItemStack> resultStack, Optional<ItemAction> resultItemAction, EnumSet<InteractionHand> hands, InteractionResult actionResult) {
        this(heldItemAction, heldItemCondition, resultItemAction, resultStack, hands, actionResult, Optional.empty());
    }

    public boolean shouldExecute(InteractionHand hand, ItemStack heldStack) {
        return doesApplyToHand(hand)
            && doesApplyToItem(heldStack);
    }

    public boolean doesApplyToHand(InteractionHand hand) {
        return hands.contains(hand);
    }

    public boolean doesApplyToItem(ItemStack heldStack) {
        return heldItemCondition
            .map(condition -> condition.test(getHolder().level(), heldStack))
            .orElse(true);
    }

    public InteractionResult getActionResult() {
        return actionResult;
    }

    protected void performActorItemStuff(Player actor, InteractionHand hand) {

        SlotAccess heldStackReference = getHeldStackReference(actor, hand);
        heldItemAction.ifPresent(action -> action.execute(actor.level(), heldStackReference));

        ItemStack resultStack = this.resultStack.isPresent()
            ? this.resultStack.get().copy()
            : heldStackReference.get().copy();

        SlotAccess resultStackReference = InventoryUtil.createStackReference(resultStack);
        boolean modified = this.resultStack.isPresent() || resultItemAction.isPresent();

        resultItemAction.ifPresent(action -> action.execute(actor.level(), resultStackReference));

        if (modified) {

            if (heldStackReference.get().isEmpty()) {
                actor.setItemInHand(hand, resultStackReference.get());
            }

            else {
                actor.getInventory().add(resultStackReference.get());
            }

        }

    }

    protected static SlotAccess getHeldStackReference(Player player, InteractionHand hand) {

        Inventory playerInventory = player.getInventory();
        int selectedSlot = playerInventory.getSelectedSlot();

        if (hand == InteractionHand.MAIN_HAND && Inventory.isHotbarSlot(selectedSlot)) {
            return SlotAccess.of(() -> playerInventory.getItem(selectedSlot), stack -> playerInventory.setItem(selectedSlot, stack));
        }

        else if (hand == InteractionHand.OFF_HAND) {
            return SlotAccess.of(() -> playerInventory.getItem(Inventory.SLOT_OFFHAND), stack -> playerInventory.setItem(Inventory.SLOT_OFFHAND, stack));
        }

        else {
            return null;
        }

    }

    public static <T extends InteractionPowerType> TypedDataObjectFactory<T> createConditionedDataFactory(SerializableData serializableData, FromData<T> fromData, BiFunction<T, SerializableData, SerializableData.Instance> toData) {
        return PowerType.createConditionedDataFactory(
            serializableData
                .add("held_item_action", ItemAction.DATA_TYPE.optional(), Optional.empty())
                .add("item_condition", ItemCondition.DATA_TYPE.optional(), Optional.empty())
                .addFunctionedDefault("held_item_condition", ItemCondition.DATA_TYPE.optional(), data -> data.get("item_condition"))
                .add("result_item_action", ItemAction.DATA_TYPE.optional(), Optional.empty())
                .add("result_stack", SerializableDataTypes.ITEM_STACK.optional(), Optional.empty())
                .add("hands", SerializableDataTypes.HAND_SET, EnumSet.allOf(InteractionHand.class))
                .add("action_result", SerializableDataTypes.ACTION_RESULT, InteractionResult.SUCCESS),
            (data, condition) -> fromData.apply(
                data,
                data.get("held_item_action"),
                data.get("held_item_condition"),
                data.get("result_item_action"),
                data.get("result_stack"),
                data.get("hands"),
                data.get("action_result"),
                condition
            ),
            (t, _serializableData) -> toData.apply(t, _serializableData)
                .set("held_item_action", t.heldItemAction)
                .set("held_item_condition", t.heldItemCondition)
                .set("result_item_action", t.resultItemAction)
                .set("result_stack", t.resultStack)
                .set("hands", t.hands)
                .set("action_result", t.actionResult)
        );
    }

    @FunctionalInterface
    public interface FromData<T extends InteractionPowerType> {
        T apply(SerializableData.Instance data, Optional<ItemAction> heldItemAction, Optional<ItemCondition> heldItemCondition, Optional<ItemAction> resultItemAction, Optional<ItemStack> resultStack, EnumSet<InteractionHand> hands, InteractionResult actionResult, Optional<EntityCondition> condition);
    }

}
