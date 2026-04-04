package io.github.apace100.apoli.action.type.entity;

import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.ItemAction;
import io.github.apace100.apoli.action.context.EntityActionContext;
import io.github.apace100.apoli.action.type.EntityActionType;
import io.github.apace100.apoli.action.type.EntityActionTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.util.InventoryUtil;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class GiveEntityActionType extends EntityActionType {

    public static final TypedDataObjectFactory<GiveEntityActionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("item_action", ItemAction.DATA_TYPE.optional(), Optional.empty())
            .add("preferred_slot", SerializableDataTypes.ATTRIBUTE_MODIFIER_SLOT.optional(), Optional.empty())
            .add("stack", SerializableDataTypes.ITEM_STACK),
        data -> new GiveEntityActionType(
            data.get("item_action"),
            data.get("preferred_slot"),
            data.get("stack")
        ),
        (actionType, serializableData) -> serializableData.instance()
            .set("item_action", actionType.itemAction)
            .set("preferred_slot", actionType.preferredSlot)
            .set("stack", actionType.stack)
    );

    private final Optional<ItemAction> itemAction;

    private final Optional<EquipmentSlotGroup> preferredSlot;
    private final ItemStack stack;

    public GiveEntityActionType(Optional<ItemAction> itemAction, Optional<EquipmentSlotGroup> preferredSlot, ItemStack stack) {
        this.itemAction = itemAction;
        this.preferredSlot = preferredSlot;
        this.stack = stack;
    }

    @Override
    public void accept(EntityActionContext context) {

        Entity entity = context.entity();

        if (entity.level().isClientSide() || stack.isEmpty()) {
            return;
        }

        SlotAccess stackReference = InventoryUtil.createStackReference(stack.copy());
        itemAction.ifPresent(action -> action.execute(entity.level(), stackReference));

        ItemStack stackToGive = stackReference.get();

        if (preferredSlot.isPresent() && entity instanceof LivingEntity living) {

            EquipmentSlotGroup actualPreferredSlot = preferredSlot.get();
            for (EquipmentSlot slot : EquipmentSlot.values()) {

                if (!actualPreferredSlot.matches(slot)) {
                    continue;
                }

                ItemStack stackInSlot = living.getItemBySlot(slot);
                if (stackInSlot.isEmpty()) {
                    living.setItemSlot(slot, stackToGive);
                    return;
                }

                else if (ItemStack.areEqual(stackInSlot, stackToGive) && stackInSlot.getCount() < stackInSlot.getMaxCount()) {

                    int itemsToGive = Math.min(stackInSlot.getMaxCount() - stackInSlot.getCount(), stackToGive.getCount());

                    stackInSlot.increment(itemsToGive);
                    stackToGive.decrement(itemsToGive);

                    if (stackToGive.isEmpty()) {
                        return;
                    }

                }

            }

        }

        if (entity instanceof Player player) {
            player.getInventory().offerOrDrop(stackToGive);
        }

        else {
            InventoryUtil.throwItem(entity, stackToGive, false , false);
        }

    }

    @Override
    public @NotNull ActionConfiguration<?> getConfig() {
        return EntityActionTypes.GIVE;
    }

}
