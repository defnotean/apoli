package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.power.PowerConfiguration;
import io.github.apace100.apoli.util.ApoliAttributes;
import io.github.apace100.apoli.util.AttributedEntityAttributeModifier;
import io.github.apace100.apoli.util.MiscUtil;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

/**
 * Modifies the entity's movement speed while in lava via the {@code apoli:lava_speed} attribute.
 * <p>Previously required the AdditionalEntityAttributes mod — now self-contained.</p>
 *
 * @deprecated Use a conditioned_attribute power with the {@code apoli:lava_speed} attribute directly.
 */
@Deprecated
public class ModifyLavaSpeedPowerType extends ConditionedAttributePowerType {

    public static final TypedDataObjectFactory<ModifyLavaSpeedPowerType> DATA_FACTORY = PowerType.createConditionedDataFactory(
        new SerializableData()
            .add("modifier", SerializableDataTypes.ATTRIBUTE_MODIFIER, null)
            .addFunctionedDefault("modifiers", SerializableDataTypes.ATTRIBUTE_MODIFIER.list(1, Integer.MAX_VALUE), data -> MiscUtil.singletonListOrNull(data.get("modifier")))
            .validate(MiscUtil.validateAnyFieldsPresent("modifier", "modifiers")),
        (data, condition) -> new ModifyLavaSpeedPowerType(
            data.get("modifiers"),
            condition
        ),
        (powerType, serializableData) -> serializableData.instance()
            .set("modifiers", powerType.attributeModifiers)
    );

    protected final List<AttributeModifier> attributeModifiers;

    public ModifyLavaSpeedPowerType(List<AttributeModifier> attributeModifiers, Optional<EntityCondition> condition) {
        super(
            attributeModifiers.stream()
                .map(attributeModifier -> new AttributedEntityAttributeModifier(ApoliAttributes.LAVA_SPEED, attributeModifier))
                .toList(),
            false, 10, condition
        );
        this.attributeModifiers = attributeModifiers;
    }

    @Override
    public @NotNull PowerConfiguration<?> getConfig() {
        return PowerTypes.MODIFY_LAVA_SPEED;
    }

}
