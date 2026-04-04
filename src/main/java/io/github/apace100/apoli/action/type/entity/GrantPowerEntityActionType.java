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
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class GrantPowerEntityActionType extends EntityActionType {

    public static final TypedDataObjectFactory<GrantPowerEntityActionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("power", ApoliDataTypes.POWER_REFERENCE)
            .add("source", SerializableDataTypes.IDENTIFIER),
        data -> new GrantPowerEntityActionType(
            data.get("power"),
            data.get("source")
        ),
        (actionType, serializableData) -> serializableData.instance()
            .set("power", actionType.power)
            .set("source", actionType.source)
    );

    private final PowerReference power;
    private final ResourceLocation source;

    public GrantPowerEntityActionType(PowerReference power, ResourceLocation source) {
        this.power = power;
        this.source = source;
    }

    @Override
    public void accept(EntityActionContext context) {
        PowerHolderComponent.grantPower(context.entity(), power.getPower(), source, true);
    }

    @Override
    public @NotNull ActionConfiguration<?> getConfig() {
        return EntityActionTypes.GRANT_POWER;
    }

}
