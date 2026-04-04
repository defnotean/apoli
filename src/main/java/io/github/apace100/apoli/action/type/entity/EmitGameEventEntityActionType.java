package io.github.apace100.apoli.action.type.entity;

import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.context.EntityActionContext;
import io.github.apace100.apoli.action.type.EntityActionType;
import io.github.apace100.apoli.action.type.EntityActionTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.core.Holder;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.NotNull;

public class EmitGameEventEntityActionType extends EntityActionType {

    public static final TypedDataObjectFactory<EmitGameEventEntityActionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("event", SerializableDataTypes.GAME_EVENT_ENTRY),
        data -> new EmitGameEventEntityActionType(
            data.get("event")
        ),
        (actionType, serializableData) -> serializableData.instance()
            .set("event", actionType.event)
    );

    private final Holder<GameEvent> event;

    public EmitGameEventEntityActionType(Holder<GameEvent> event) {
        this.event = event;
    }

    @Override
    public void accept(EntityActionContext context) {
        context.entity().emitGameEvent(event);
    }

    @Override
    public @NotNull ActionConfiguration<?> getConfig() {
        return EntityActionTypes.EMIT_GAME_EVENT;
    }

}
