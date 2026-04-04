package io.github.apace100.apoli.condition.type.entity;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.context.EntityConditionContext;
import io.github.apace100.apoli.condition.type.EntityConditionType;
import io.github.apace100.apoli.condition.type.EntityConditionTypes;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.power.PowerReference;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Function;

public class PowerEntityConditionType extends EntityConditionType {

    public static final TypedDataObjectFactory<PowerEntityConditionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("power", ApoliDataTypes.POWER_REFERENCE)
            .add("source", SerializableDataTypes.IDENTIFIER.optional(), Optional.empty()),
        data -> new PowerEntityConditionType(
            data.get("power"),
            data.get("source")
        ),
        (conditionType, serializableData) -> serializableData.instance()
            .set("power", conditionType.power)
            .set("source", conditionType.source)
    );

    private final PowerReference power;
    private final Optional<Identifier> source;

    private final Function<PowerHolderComponent, Boolean> powerChecker;

    public PowerEntityConditionType(PowerReference power, Optional<Identifier> source) {
        this.power = power;
        this.source = source;
        this.powerChecker = component -> source
            .map(id -> component.hasPower(power, id))
            .orElseGet(() -> component.hasPower(power));
    }

    @Override
    public boolean test(EntityConditionContext context) {
        return PowerHolderComponent.getOptional(context.entity())
            .map(powerChecker)
            .orElse(false);
    }

    @Override
    public @NotNull ConditionConfiguration<?> getConfig() {
        return EntityConditionTypes.POWER;
    }

}
