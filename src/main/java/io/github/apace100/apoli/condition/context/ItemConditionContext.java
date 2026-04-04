package io.github.apace100.apoli.condition.context;

import io.github.apace100.apoli.util.context.ConditionContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public record ItemConditionContext(Level world, ItemStack stack) implements ConditionContext {

}
