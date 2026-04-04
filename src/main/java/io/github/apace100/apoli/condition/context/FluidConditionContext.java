package io.github.apace100.apoli.condition.context;

import io.github.apace100.apoli.util.context.ConditionContext;
import net.minecraft.world.level.material.FluidState;

public record FluidConditionContext(FluidState fluidState) implements ConditionContext {

}
