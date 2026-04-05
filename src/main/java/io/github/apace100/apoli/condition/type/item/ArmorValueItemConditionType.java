package io.github.apace100.apoli.condition.type.item;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.context.ItemConditionContext;
import io.github.apace100.apoli.condition.type.ItemConditionType;
import io.github.apace100.apoli.condition.type.ItemConditionTypes;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.util.Comparison;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ArmorValueItemConditionType extends ItemConditionType {

    public static final TypedDataObjectFactory<ArmorValueItemConditionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("comparison", ApoliDataTypes.COMPARISON)
            .add("compare_to", SerializableDataTypes.INT),
        data -> new ArmorValueItemConditionType(
            data.get("comparison"),
            data.get("compare_to")
        ),
        (conditionType, serializableData) -> serializableData.instance()
            .set("comparison", conditionType.comparison)
            .set("compare_to", conditionType.compareTo)
    );

    private final Comparison comparison;
    private final int compareTo;

    public ArmorValueItemConditionType(Comparison comparison, int compareTo) {
        this.comparison = comparison;
        this.compareTo = compareTo;
    }

    @Override
    public boolean test(ItemConditionContext context) {
        ItemStack stack = context.stack();
        var armorAttr = stack.get(DataComponents.ATTRIBUTE_MODIFIERS);
        if (armorAttr == null) return comparison.compare(0, compareTo);
        // Sum up armor attribute modifiers to get effective protection
        // In 26.1 armor value is tracked through attribute modifiers
        return comparison.compare(0, compareTo); // TODO: Adapt to new armor component system
    }

    @Override
    public @NotNull ConditionConfiguration<?> getConfig() {
        return ItemConditionTypes.ARMOR_VALUE;
    }

}
