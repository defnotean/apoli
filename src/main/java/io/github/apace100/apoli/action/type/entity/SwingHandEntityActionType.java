package io.github.apace100.apoli.action.type.entity;

import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.context.EntityActionContext;
import io.github.apace100.apoli.action.type.EntityActionType;
import io.github.apace100.apoli.action.type.EntityActionTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.InteractionHand;
import org.jetbrains.annotations.NotNull;

public class SwingHandEntityActionType extends EntityActionType {

    public static final TypedDataObjectFactory<SwingHandEntityActionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("hand", SerializableDataTypes.HAND, InteractionHand.MAIN_HAND),
        data -> new SwingHandEntityActionType(
            data.get("hand")
        ),
        (actionType, serializableData) -> serializableData.instance()
            .set("hand", actionType.hand)
    );

    private final InteractionHand hand;

    public SwingHandEntityActionType(InteractionHand hand) {
        this.hand = hand;
    }

    @Override
    public void accept(EntityActionContext context) {

        if (context.entity() instanceof LivingEntity livingEntity) {
            livingEntity.swing(hand, true);
        }

    }

    @Override
    public @NotNull ActionConfiguration<?> getConfig() {
        return EntityActionTypes.SWING_HAND;
    }

}
