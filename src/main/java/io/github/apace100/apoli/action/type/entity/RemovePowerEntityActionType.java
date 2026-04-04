package io.github.apace100.apoli.action.type.entity;

import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.context.EntityActionContext;
import io.github.apace100.apoli.action.type.EntityActionType;
import io.github.apace100.apoli.action.type.EntityActionTypes;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.power.PowerReference;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.world.entity.Entity;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public class RemovePowerEntityActionType extends EntityActionType {

    public static final TypedDataObjectFactory<RemovePowerEntityActionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("power", ApoliDataTypes.POWER_REFERENCE),
        data -> new RemovePowerEntityActionType(
            data.get("power")
        ),
        (actionType, serializableData) -> serializableData.instance()
            .set("power", actionType.power)
    );

    private final PowerReference power;

    public RemovePowerEntityActionType(PowerReference power) {
        this.power = power;
    }

    @Override
    public void accept(EntityActionContext context) {

        Entity entity = context.entity();
        List<ResourceLocation> sources = PowerHolderComponent.getOptional(entity)
            .stream()
            .map(component -> component.getSources(power))
            .flatMap(Collection::stream)
            .toList();

        if (!sources.isEmpty()) {
            PowerHolderComponent.revokeAllPowersFromAllSources(entity, sources, true);
        }

    }

    @Override
    public @NotNull ActionConfiguration<?> getConfig() {
        return EntityActionTypes.REMOVE_POWER;
    }

}
