package io.github.apace100.apoli.condition.context;

import io.github.apace100.apoli.util.context.ConditionContext;
import net.minecraft.world.damagesource.DamageSource;

public record DamageConditionContext(DamageSource source, float amount) implements ConditionContext {

}
