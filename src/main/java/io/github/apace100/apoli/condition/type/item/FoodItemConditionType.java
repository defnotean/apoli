package io.github.apace100.apoli.condition.type.item;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.context.ItemConditionContext;
import io.github.apace100.apoli.condition.type.ItemConditionType;
import io.github.apace100.apoli.condition.type.ItemConditionTypes;
import io.github.apace100.apoli.power.type.EdibleItemPowerType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class FoodItemConditionType extends ItemConditionType {

    @Override
    public boolean test(ItemConditionContext context) {
        ItemStack stack = context.stack();
        return EdibleItemPowerType.get(stack).isPresent()
            || stack.contains(DataComponents.FOOD);
    }

    @Override
    public @NotNull ConditionConfiguration<?> getConfig() {
        return ItemConditionTypes.FOOD;
    }

}
