package io.github.apace100.apoli.condition.type.item;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.context.ItemConditionContext;
import io.github.apace100.apoli.condition.type.ItemConditionType;
import io.github.apace100.apoli.condition.type.ItemConditionTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class SmeltableItemConditionType extends ItemConditionType {

    @Override
    public boolean test(ItemConditionContext context) {

        ItemStack stack = context.stack();
        Level world = context.world();

        return world.getRecipeManager()
            .getFirstMatch(RecipeType.SMELTING, new SingleRecipeInput(stack), world)
            .isPresent();

    }

    @Override
    public @NotNull ConditionConfiguration<?> getConfig() {
        return ItemConditionTypes.SMELTABLE;
    }

}
