package io.github.apace100.apoli.condition.context;

import io.github.apace100.apoli.util.context.ConditionContext;
import net.minecraft.world.entity.Entity;

public record BiEntityConditionContext(Entity actor, Entity target) implements ConditionContext {

}
