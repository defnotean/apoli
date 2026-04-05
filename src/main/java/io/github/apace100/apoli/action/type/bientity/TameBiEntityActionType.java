package io.github.apace100.apoli.action.type.bientity;

import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.context.BiEntityActionContext;
import io.github.apace100.apoli.action.type.BiEntityActionType;
import io.github.apace100.apoli.action.type.BiEntityActionTypes;
import io.github.apace100.apoli.util.requirement.BiEntityRequirement;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public class TameBiEntityActionType extends BiEntityActionType {

    @Override
    public void accept(BiEntityActionContext context) {

        if (context.actor() instanceof Player playerActor) {

            switch (context.target()) {
                case TamableAnimal tameableTarget when !tameableTarget.isTamed() ->
                    tameableTarget.setOwner(playerActor);
                case AbstractHorse horseLikeTarget when !horseLikeTarget.isTame() ->
                    horseLikeTarget.bondWithPlayer(playerActor);
                default -> {

                }
            }

        }

    }

    @Override
    public @NotNull ActionConfiguration<?> getConfig() {
        return BiEntityActionTypes.TAME;
    }

    @Override
    public BiEntityRequirement getRequirement() {
        return BiEntityRequirement.BOTH;
    }

}
