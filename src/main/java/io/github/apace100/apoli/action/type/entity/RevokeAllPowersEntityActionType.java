package io.github.apace100.apoli.action.type.entity;

import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.context.EntityActionContext;
import io.github.apace100.apoli.action.type.EntityActionType;
import io.github.apace100.apoli.action.type.EntityActionTypes;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class RevokeAllPowersEntityActionType extends EntityActionType {

    public static final TypedDataObjectFactory<RevokeAllPowersEntityActionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("source", SerializableDataTypes.IDENTIFIER),
        data -> new RevokeAllPowersEntityActionType(
            data.get("source")
        ),
        (actionType, serializableData) -> serializableData.instance()
            .set("source", actionType.source)
    );

    private final ResourceLocation source;

    public RevokeAllPowersEntityActionType(ResourceLocation source) {
        this.source = source;
    }

    @Override
    public void accept(EntityActionContext context) {
        PowerHolderComponent.revokeAllPowersFromSource(context.entity(), source, true);
    }

    @Override
    public @NotNull ActionConfiguration<?> getConfig() {
        return EntityActionTypes.REVOKE_ALL_POWERS;
    }

}
