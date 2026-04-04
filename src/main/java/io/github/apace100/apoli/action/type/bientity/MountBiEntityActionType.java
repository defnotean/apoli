package io.github.apace100.apoli.action.type.bientity;

import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.context.BiEntityActionContext;
import io.github.apace100.apoli.action.type.BiEntityActionType;
import io.github.apace100.apoli.action.type.BiEntityActionTypes;
import io.github.apace100.apoli.networking.packet.s2c.MountPlayerS2CPacket;
import io.github.apace100.apoli.util.requirement.BiEntityRequirement;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

public class MountBiEntityActionType extends BiEntityActionType {

    @Override
    public void accept(BiEntityActionContext context) {

        Entity actor = context.actor();
        Entity target = context.target();

        actor.startRiding(target, true);

        if (target instanceof ServerPlayer targetPlayer) {
            ServerPlayNetworking.send(targetPlayer, new MountPlayerS2CPacket(actor.getId(), target.getId()));
        }

    }

    @Override
    public @NotNull ActionConfiguration<?> getConfig() {
        return BiEntityActionTypes.MOUNT;
    }

    @Override
    public BiEntityRequirement getRequirement() {
        return BiEntityRequirement.BOTH;
    }

}
