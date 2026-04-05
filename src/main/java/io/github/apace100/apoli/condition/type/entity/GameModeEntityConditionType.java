package io.github.apace100.apoli.condition.type.entity;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.context.EntityConditionContext;
import io.github.apace100.apoli.condition.type.EntityConditionType;
import io.github.apace100.apoli.condition.type.EntityConditionTypes;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.mixin.ClientPlayerEntityAccessor;
import io.github.apace100.apoli.mixin.ClientPlayerInteractionManagerAccessor;
import io.github.apace100.apoli.mixin.ServerPlayerInteractionManagerAccessor;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import org.jetbrains.annotations.NotNull;

public class GameModeEntityConditionType extends EntityConditionType {

    public static final TypedDataObjectFactory<GameModeEntityConditionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("gamemode", ApoliDataTypes.GAME_MODE),
        data -> new GameModeEntityConditionType(
            data.get("gamemode")
        ),
        (conditionType, serializableData) -> serializableData.instance()
            .set("gamemode", conditionType.gameMode)
    );

    private final GameType gameMode;

    public GameModeEntityConditionType(GameType gameMode) {
        this.gameMode = gameMode;
    }

    @Override
    public boolean test(EntityConditionContext context) {

        if (!(context.entity() instanceof Player player)) {
            return false;
        }

        else if (player instanceof ServerPlayer serverPlayer) {
            ServerPlayerInteractionManagerAccessor interactionManager = (ServerPlayerInteractionManagerAccessor) serverPlayer.gameMode;
            return interactionManager.getGameMode() == gameMode;
        }

        else if (player instanceof LocalPlayer clientPlayer) {
            ClientPlayerInteractionManagerAccessor interactionManager = (ClientPlayerInteractionManagerAccessor) (((ClientPlayerEntityAccessor) clientPlayer).getClient()).gameMode;
            return interactionManager != null && interactionManager.getGameMode() == gameMode;
        }

        else {
            return false;
        }

    }

    @Override
    public @NotNull ConditionConfiguration<?> getConfig() {
        return EntityConditionTypes.GAME_MODE;
    }

}
