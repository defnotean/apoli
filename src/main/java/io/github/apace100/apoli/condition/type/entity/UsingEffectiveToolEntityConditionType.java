package io.github.apace100.apoli.condition.type.entity;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.context.EntityConditionContext;
import io.github.apace100.apoli.condition.type.EntityConditionType;
import io.github.apace100.apoli.condition.type.EntityConditionTypes;
import io.github.apace100.apoli.mixin.ClientPlayerEntityAccessor;
import io.github.apace100.apoli.mixin.ClientPlayerInteractionManagerAccessor;
import io.github.apace100.apoli.mixin.ServerPlayerInteractionManagerAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

public class UsingEffectiveToolEntityConditionType extends EntityConditionType {

    @Override
    public boolean test(EntityConditionContext context) {

        if (!(context.entity() instanceof Player playerEntity)) {
            return false;
        }

        BlockState miningBlockState;
        if (playerEntity instanceof ServerPlayer serverPlayer) {

            ServerPlayerInteractionManagerAccessor interactionManager = (ServerPlayerInteractionManagerAccessor) serverPlayer.gameMode;
            if (!interactionManager.getMining()) {
                return false;
            }

            miningBlockState = playerEntity.level().getBlockState(interactionManager.getMiningPos());

        }

        else if (playerEntity instanceof LocalPlayer clientPlayer) {

            ClientPlayerInteractionManagerAccessor interactionManager = (ClientPlayerInteractionManagerAccessor) ((ClientPlayerEntityAccessor) clientPlayer).getClient().gameMode;
            if (interactionManager == null || !interactionManager.getBreakingBlock()) {
                return false;
            }

            miningBlockState = playerEntity.level().getBlockState(interactionManager.getCurrentBreakingPos());

        }

        else {
            return false;
        }

        return playerEntity.hasCorrectToolForDrops(miningBlockState);

    }

    @Override
    public @NotNull ConditionConfiguration<?> getConfig() {
        return EntityConditionTypes.USING_EFFECTIVE_TOOL;
    }

}
