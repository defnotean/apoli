package io.github.apace100.apoli.condition.type.fluid;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.context.FluidConditionContext;
import io.github.apace100.apoli.condition.type.FluidConditionType;
import io.github.apace100.apoli.condition.type.FluidConditionTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.NotNull;

public class FluidFluidConditionType extends FluidConditionType {

    public static final TypedDataObjectFactory<FluidFluidConditionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("fluid", SerializableDataTypes.FLUID),
        data -> new FluidFluidConditionType(
            data.get("fluid")
        ),
        (conditionType, serializableData) -> serializableData.instance()
            .set("fluid", conditionType.fluid)
    );

    private final Fluid fluid;

    public FluidFluidConditionType(Fluid fluid) {
        this.fluid = fluid;
    }

    @Override
    public boolean test(FluidConditionContext context) {
        return context.fluidState().is(fluid);
    }

    @Override
    public @NotNull ConditionConfiguration<?> getConfig() {
        return FluidConditionTypes.FLUID;
    }

}
