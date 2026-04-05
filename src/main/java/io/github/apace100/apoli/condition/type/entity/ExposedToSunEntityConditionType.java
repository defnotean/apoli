package io.github.apace100.apoli.condition.type.entity;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.context.EntityConditionContext;
import io.github.apace100.apoli.condition.type.EntityConditionType;
import io.github.apace100.apoli.condition.type.EntityConditionTypes;
import io.github.apace100.apoli.util.Comparison;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

public class ExposedToSunEntityConditionType extends EntityConditionType {

    private static final InRainEntityConditionType IN_RAIN = new InRainEntityConditionType();
    private static final BrightnessEntityConditionType BRIGHTNESS = new BrightnessEntityConditionType(Comparison.GREATER_THAN, 0.5F);
    private static final ExposedToSkyEntityConditionType EXPOSED_TO_SKY = new ExposedToSkyEntityConditionType();

    @Override
    public boolean test(EntityConditionContext context) {
        Entity entity = context.entity();
        return entity.level().isBrightOutside()
            && !IN_RAIN.test(context)
            && BRIGHTNESS.test(context)
            && EXPOSED_TO_SKY.test(context);
    }

    @Override
    public @NotNull ConditionConfiguration<?> getConfig() {
        return EntityConditionTypes.EXPOSED_TO_SUN;
    }

}
