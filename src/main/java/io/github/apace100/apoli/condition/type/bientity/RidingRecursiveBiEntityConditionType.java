package io.github.apace100.apoli.condition.type.bientity;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.context.BiEntityConditionContext;
import io.github.apace100.apoli.condition.type.BiEntityConditionType;
import io.github.apace100.apoli.condition.type.BiEntityConditionTypes;
import io.github.apace100.apoli.util.requirement.BiEntityRequirement;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class RidingRecursiveBiEntityConditionType extends BiEntityConditionType {

    @Override
    public boolean test(BiEntityConditionContext context) {

        Entity vehicle = context.actor().getVehicle();

        while (vehicle != null) {

            if (Objects.equals(vehicle, context.target())) {
                return true;
            }

            else {
                vehicle = vehicle.getVehicle();
            }

        }

        return false;

    }

    @Override
    public BiEntityRequirement getRequirement() {
        return BiEntityRequirement.BOTH;
    }

    @Override
    public @NotNull ConditionConfiguration<?> getConfig() {
        return BiEntityConditionTypes.RIDING_RECURSIVE;
    }

}
