package io.github.apace100.apoli.condition.type.entity;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.context.EntityConditionContext;
import io.github.apace100.apoli.condition.type.EntityConditionType;
import io.github.apace100.apoli.condition.type.EntityConditionTypes;
import io.github.apace100.apoli.mixin.EntityAccessor;
import org.jetbrains.annotations.NotNull;

public class InRainEntityConditionType extends EntityConditionType {

    @Override
    public boolean test(EntityConditionContext context) {
        return ((EntityAccessor) context.entity()).callIsInRain();
    }

    @Override
    public @NotNull ConditionConfiguration<?> getConfig() {
        return EntityConditionTypes.IN_RAIN;
    }

}
