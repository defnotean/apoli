package io.github.apace100.apoli.action.type.entity;

import io.github.apace100.apoli.access.CustomToastViewer;
import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.context.EntityActionContext;
import io.github.apace100.apoli.action.type.EntityActionType;
import io.github.apace100.apoli.action.type.EntityActionTypes;
import io.github.apace100.apoli.data.CustomToastData;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

public class ShowToastEntityActionType extends EntityActionType {

    public static final TypedDataObjectFactory<ShowToastEntityActionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        CustomToastData.FACTORY.getSerializableData().copy(),
        data -> new ShowToastEntityActionType(
            CustomToastData.FACTORY.fromData(data)
        ),
        (actionType, serializableData) ->
            CustomToastData.FACTORY.toData(actionType.customToastData, serializableData)
    );

    private final CustomToastData customToastData;

    public ShowToastEntityActionType(CustomToastData customToastData) {
        this.customToastData = customToastData;
    }

    @Override
    public void accept(EntityActionContext context) {

        Entity entity = context.entity();

        if (!entity.level().isClientSide() && entity instanceof CustomToastViewer viewer) {
            viewer.apoli$showToast(customToastData);
        }

    }

    @Override
    public @NotNull ActionConfiguration<?> getConfig() {
        return EntityActionTypes.SHOW_TOAST;
    }

}
