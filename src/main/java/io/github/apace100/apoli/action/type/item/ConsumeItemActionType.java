package io.github.apace100.apoli.action.type.item;

import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.context.ItemActionContext;
import io.github.apace100.apoli.action.type.ItemActionType;
import io.github.apace100.apoli.action.type.ItemActionTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import org.jetbrains.annotations.NotNull;

public class ConsumeItemActionType extends ItemActionType {

    public static final TypedDataObjectFactory<ConsumeItemActionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("amount", SerializableDataTypes.INT, 1),
        data -> new ConsumeItemActionType(
            data.get("amount")
        ),
        (actionType, serializableData) -> serializableData.instance()
            .set("amount", actionType.amount)
    );

    private final int amount;

    public ConsumeItemActionType(int amount) {
        this.amount = amount;
    }

    @Override
    public void accept(ItemActionContext context) {
        context.stackReference().get().shrink(amount);
    }

    @Override
    public @NotNull ActionConfiguration<?> getConfig() {
        return ItemActionTypes.CONSUME;
    }

}
