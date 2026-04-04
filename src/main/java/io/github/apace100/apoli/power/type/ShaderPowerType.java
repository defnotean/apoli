package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.power.PowerConfiguration;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class ShaderPowerType extends PowerType implements Prioritized<ShaderPowerType> {

    public static final TypedDataObjectFactory<ShaderPowerType> DATA_FACTORY = PowerType.createConditionedDataFactory(
        new SerializableData()
            .add("shader", SerializableDataTypes.IDENTIFIER)
            .add("toggleable", SerializableDataTypes.BOOLEAN, true)
            .add("priority", SerializableDataTypes.INT, 0),
        (data, condition) -> new ShaderPowerType(
            data.get("shader"),
            data.get("toggleable"),
            data.get("priority"),
            condition
        ),
        (powerType, serializableData) -> serializableData.instance()
            .set("shader", powerType.getShaderLocation())
            .set("toggleable", powerType.isToggleable())
            .set("priority", powerType.getPriority())
    );

    private final ResourceLocation shaderLocation;

    private final boolean toggleable;
    private final int priority;

    public ShaderPowerType(ResourceLocation shaderLocation, boolean toggleable, int priority, Optional<EntityCondition> condition) {
        super(condition);
        this.shaderLocation = shaderLocation;
        this.toggleable = toggleable;
        this.priority = priority;
    }

    @Override
    public @NotNull PowerConfiguration<?> getConfig() {
        return PowerTypes.SHADER;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    public ResourceLocation getShaderLocation() {
        return shaderLocation;
    }

    public boolean isToggleable() {
        return toggleable;
    }

}
