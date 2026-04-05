package io.github.apace100.apoli.action.type.bientity;

import io.github.apace100.apoli.access.CustomLeashable;
import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.context.BiEntityActionContext;
import io.github.apace100.apoli.action.type.BiEntityActionType;
import io.github.apace100.apoli.action.type.BiEntityActionTypes;
import io.github.apace100.apoli.util.requirement.BiEntityRequirement;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Leashable;
import org.jetbrains.annotations.NotNull;

public class LeashBiEntityActionType extends BiEntityActionType {

    @Override
    public void accept(BiEntityActionContext context) {

        Entity actor = context.actor();
        Entity target = context.target();

        if (target instanceof Leashable leashable && target instanceof CustomLeashable customLeashable && !leashable.isLeashed()) {
            customLeashable.apoli$setCustomLeashed(true);
            leashable.setLeashedTo(actor, true);
        }

    }

    @Override
    public @NotNull ActionConfiguration<?> getConfig() {
        return BiEntityActionTypes.LEASH;
    }

    @Override
    public BiEntityRequirement getRequirement() {
        return BiEntityRequirement.BOTH;
    }

}
