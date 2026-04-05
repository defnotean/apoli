package io.github.apace100.apoli.action.type.entity;

import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.ItemAction;
import io.github.apace100.apoli.action.context.EntityActionContext;
import io.github.apace100.apoli.action.type.EntityActionType;
import io.github.apace100.apoli.action.type.EntityActionTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import org.jetbrains.annotations.NotNull;

public class EquippedItemActionEntityActionType extends EntityActionType {

    public static final TypedDataObjectFactory<EquippedItemActionEntityActionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("equipment_slot", SerializableDataTypes.EQUIPMENT_SLOT_GROUP)
            .add("item_action", ItemAction.DATA_TYPE),
        data -> new EquippedItemActionEntityActionType(
            data.get("equipment_slot"),
            data.get("item_action")
        ),
        (actionType, serializableData) -> serializableData.instance()
            .set("equipment_slot", actionType.equipmentSlot)
            .set("item_action", actionType.itemAction)
    );

    private final EquipmentSlotGroup equipmentSlot;
    private final ItemAction itemAction;

    public EquippedItemActionEntityActionType(EquipmentSlotGroup equipmentSlot, ItemAction itemAction) {
        this.equipmentSlot = equipmentSlot;
        this.itemAction = itemAction;
    }

    @Override
    public void accept(EntityActionContext context) {

        if (!(context.entity() instanceof LivingEntity livingEntity)) {
            return;
        }

        for (EquipmentSlot slot : EquipmentSlot.values()) {

            if (equipmentSlot.test(slot)) {
                itemAction.execute(livingEntity.level(), SlotAccess.of(livingEntity, slot));
            }

        }

    }

    @Override
    public @NotNull ActionConfiguration<?> getConfig() {
        return EntityActionTypes.EQUIPPED_ITEM_ACTION;
    }

}
