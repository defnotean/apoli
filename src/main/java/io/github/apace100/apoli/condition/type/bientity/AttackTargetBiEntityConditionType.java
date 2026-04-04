package io.github.apace100.apoli.condition.type.bientity;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.context.BiEntityConditionContext;
import io.github.apace100.apoli.condition.type.BiEntityConditionType;
import io.github.apace100.apoli.condition.type.BiEntityConditionTypes;
import io.github.apace100.apoli.util.requirement.BiEntityRequirement;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Targeter;
import net.minecraft.world.entity.monster.Angerable;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class AttackTargetBiEntityConditionType extends BiEntityConditionType {

    @Override
    public boolean test(BiEntityConditionContext context) {

        Entity actor = context.actor();
        Entity target = context.target();

        return (actor instanceof Targeter targeterActor && Objects.equals(target, targeterActor.getTarget()))
            || (actor instanceof Angerable angerableActor && Objects.equals(target, angerableActor.getTarget()));

    }

    @Override
    public BiEntityRequirement getRequirement() {
        return BiEntityRequirement.BOTH;
    }

    @Override
    public @NotNull ConditionConfiguration<?> getConfig() {
        return BiEntityConditionTypes.ATTACK_TARGET;
    }

}
