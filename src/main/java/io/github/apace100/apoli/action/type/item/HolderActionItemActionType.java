package io.github.apace100.apoli.action.type.item;

import io.github.apace100.apoli.access.EntityLinkedItemStack;
import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.EntityAction;
import io.github.apace100.apoli.action.context.ItemActionContext;
import io.github.apace100.apoli.action.type.ItemActionType;
import io.github.apace100.apoli.action.type.ItemActionTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

public class HolderActionItemActionType extends ItemActionType {

    public static final TypedDataObjectFactory<HolderActionItemActionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("action", EntityAction.DATA_TYPE),
        data -> new HolderActionItemActionType(
            data.get("action")
        ),
        (actionType, serializableData) -> serializableData.instance()
            .set("action", actionType.entityAction)
    );

    private final EntityAction entityAction;

    public HolderActionItemActionType(EntityAction entityAction) {
        this.entityAction = entityAction;
    }

    @Override
    public void accept(ItemActionContext context) {

        if (context.stackReference().get() instanceof EntityLinkedItemStack linkedItemStack) {

            Entity holder = linkedItemStack.apoli$getEntity(true);

            if (holder != null) {
                entityAction.execute(holder);
            }

        }

    }

    @Override
    public @NotNull ActionConfiguration<?> getConfig() {
        return ItemActionTypes.HOLDER;
    }

}
