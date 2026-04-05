package io.github.apace100.apoli.action.type.entity;

import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.context.EntityActionContext;
import io.github.apace100.apoli.action.type.EntityActionType;
import io.github.apace100.apoli.action.type.EntityActionTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.util.modifier.Modifier;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.ServerStatsCounter;
import net.minecraft.stats.Stat;
import org.jetbrains.annotations.NotNull;

public class ModifyStatEntityActionType extends EntityActionType {

    public static final TypedDataObjectFactory<ModifyStatEntityActionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("stat", SerializableDataTypes.STAT)
            .add("modifier", Modifier.DATA_TYPE),
        data -> new ModifyStatEntityActionType(
            data.get("stat"),
            data.get("modifier")
        ),
        (actionType, serializableData) -> serializableData.instance()
            .set("stat", actionType.stat)
            .set("modifier", actionType.modifier)
    );

    private final Stat<?> stat;
    private final Modifier modifier;

    public ModifyStatEntityActionType(Stat<?> stat, Modifier modifier) {
        this.stat = stat;
        this.modifier = modifier;
    }

    @Override
    public void accept(EntityActionContext context) {

        if (context.entity() instanceof ServerPlayer serverPlayer) {

            ServerStatsCounter statHandler = serverPlayer.getStats();
            int originalValue = statHandler.getValue(stat);

            serverPlayer.resetStat(stat);
            serverPlayer.awardStat(stat, (int) modifier.apply(serverPlayer, originalValue));

        }

    }

    @Override
    public @NotNull ActionConfiguration<?> getConfig() {
        return EntityActionTypes.MODIFY_STAT;
    }

}
