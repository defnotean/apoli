package io.github.apace100.apoli.condition.type.item;

import io.github.apace100.apoli.component.item.ApoliDataComponentTypes;
import io.github.apace100.apoli.component.item.ItemPowersComponent;
import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.context.ItemConditionContext;
import io.github.apace100.apoli.condition.type.ItemConditionType;
import io.github.apace100.apoli.condition.type.ItemConditionTypes;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.util.Comparison;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.world.entity.EquipmentSlotGroup;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class PowerCountItemConditionType extends ItemConditionType {

    public static final TypedDataObjectFactory<PowerCountItemConditionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("slot", SerializableDataTypes.ATTRIBUTE_MODIFIER_SLOT.optional(), Optional.empty())
            .add("comparison", ApoliDataTypes.COMPARISON)
            .add("compare_to", SerializableDataTypes.INT),
        data -> new PowerCountItemConditionType(
            data.get("slot"),
            data.get("comparison"),
            data.get("compare_to")
        ),
        (conditionType, serializableData) -> serializableData.instance()
            .set("slot", conditionType.slot)
            .set("comparison", conditionType.comparison)
            .set("compare_to", conditionType.compareTo)
    );

    private final Optional<EquipmentSlotGroup> slot;

    private final Comparison comparison;
    private final int compareTo;

    public PowerCountItemConditionType(Optional<EquipmentSlotGroup> slot, Comparison comparison, int compareTo) {
        this.slot = slot;
        this.comparison = comparison;
        this.compareTo = compareTo;
    }

    @Override
    public boolean test(ItemConditionContext context) {

        ItemPowersComponent itemPowers = context.stack().getOrDefault(ApoliDataComponentTypes.POWERS, ItemPowersComponent.DEFAULT);
        int powerCount = slot
            .map(itemPowers::matchingSlots)
            .orElseGet(itemPowers::size);

        return comparison.compare(powerCount, compareTo);

    }

    @Override
    public @NotNull ConditionConfiguration<?> getConfig() {
        return ItemConditionTypes.POWER_COUNT;
    }

}
