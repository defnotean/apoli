package io.github.apace100.apoli.action.type.entity;

import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.context.EntityActionContext;
import io.github.apace100.apoli.action.type.EntityActionType;
import io.github.apace100.apoli.action.type.EntityActionTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import org.jetbrains.annotations.NotNull;

public class SetOnFireEntityActionType extends EntityActionType {

    public static final TypedDataObjectFactory<SetOnFireEntityActionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("duration", SerializableDataTypes.POSITIVE_FLOAT),
        data -> new SetOnFireEntityActionType(
            data.get("duration")
        ),
        (actionType, serializableData) -> serializableData.instance()
            .set("duration", actionType.duration)
    );

    private final float duration;

    public SetOnFireEntityActionType(float duration) {
        this.duration = duration;
    }

    @Override
    public void accept(EntityActionContext context) {
        context.entity().igniteForSeconds(duration);
    }

    @Override
    public @NotNull ActionConfiguration<?> getConfig() {
        return EntityActionTypes.SET_ON_FIRE;
    }

}
