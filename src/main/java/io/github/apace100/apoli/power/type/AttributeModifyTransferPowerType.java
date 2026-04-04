package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.power.PowerConfiguration;
import io.github.apace100.apoli.registry.ApoliClassData;
import io.github.apace100.apoli.util.modifier.Modifier;
import io.github.apace100.apoli.util.modifier.ModifierUtil;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.core.Holder;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class AttributeModifyTransferPowerType extends PowerType {

    public static final TypedDataObjectFactory<AttributeModifyTransferPowerType> DATA_FACTORY = PowerType.createConditionedDataFactory(
        new SerializableData()
            .add("class", ApoliClassData.POWER_TYPE.getDataType())
            .add("attribute", SerializableDataTypes.ATTRIBUTE_ENTRY)
            .add("multiplier", SerializableDataTypes.DOUBLE, 1.0D),
        (data, condition) -> new AttributeModifyTransferPowerType(
            data.get("class"),
            data.get("attribute"),
            data.get("multiplier"),
            condition
        ),
        (powerType, serializableData) -> serializableData.instance()
            .set("class", powerType.modifyClass)
            .set("attribute", powerType.attribute)
            .set("multiplier", powerType.valueMultiplier)
    );

    private final Class<?> modifyClass;
    private final Holder<Attribute> attribute;

    private final double valueMultiplier;

    public AttributeModifyTransferPowerType(Class<?> modifyClass, Holder<Attribute> attribute, double valueMultiplier, Optional<EntityCondition> condition) {
        super(condition);
        this.modifyClass = modifyClass;
        this.attribute = attribute;
        this.valueMultiplier = valueMultiplier;
    }

    @Override
    public @NotNull PowerConfiguration<?> getConfig() {
        return PowerTypes.ATTRIBUTE_MODIFY_TRANSFER;
    }

    public boolean doesApply(Class<?> cls) {
        return cls.equals(modifyClass);
    }

    public void addModifiers(List<Modifier> modifiers) {

        AttributeMap attributeContainer = getHolder().getAttributes();
        AttributeInstance attributeInstance = attributeContainer.getCustomInstance(attribute);

        if (attributeInstance == null) {
            return;
        }

        attributeInstance.getModifiers()
            .stream()
            .map(mod -> new AttributeModifier(mod.id(), mod.value() * valueMultiplier, mod.operation()))
            .map(ModifierUtil::fromAttributeModifier)
            .forEach(modifiers::add);

    }

}
