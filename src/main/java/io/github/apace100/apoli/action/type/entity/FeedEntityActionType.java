package io.github.apace100.apoli.action.type.entity;

import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.context.EntityActionContext;
import io.github.apace100.apoli.action.type.EntityActionType;
import io.github.apace100.apoli.action.type.EntityActionTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public class FeedEntityActionType extends EntityActionType {

    public static final TypedDataObjectFactory<FeedEntityActionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("nutrition", SerializableDataTypes.INT)
            .add("saturation", SerializableDataTypes.FLOAT),
        data -> new FeedEntityActionType(
            data.get("nutrition"),
            data.get("saturation")
        ),
        (actionType, serializableData) -> serializableData.instance()
            .set("nutrition", actionType.nutrition)
            .set("saturation", actionType.saturation)
    );

    private final int nutrition;
    private final float saturation;

    public FeedEntityActionType(int nutrition, float saturation) {
        this.nutrition = nutrition;
        this.saturation = saturation;
    }

    @Override
    public void accept(EntityActionContext context) {

        if (context.entity() instanceof Player player) {
            player.getFoodData().eat(nutrition, saturation);
        }

    }

    @Override
    public @NotNull ActionConfiguration<?> getConfig() {
        return EntityActionTypes.FEED;
    }

}
