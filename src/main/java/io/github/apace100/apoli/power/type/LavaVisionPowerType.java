package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.power.PowerConfiguration;
import io.github.apace100.apoli.util.ApoliAttributes;
import io.github.apace100.apoli.util.AttributedEntityAttributeModifier;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Grants lava vision by modifying the {@code apoli:lava_visibility} attribute.
 * <p>Previously required the AdditionalEntityAttributes mod — now self-contained.</p>
 *
 * @deprecated Use a conditioned_attribute power with the {@code apoli:lava_visibility} attribute directly.
 */
@Deprecated
public class LavaVisionPowerType extends PowerType implements AttributeModifying {

    public static final TypedDataObjectFactory<LavaVisionPowerType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("v", SerializableDataTypes.FLOAT),
        data -> new LavaVisionPowerType(
            data.get("v")
        ),
        (powerType, serializableData) -> serializableData.instance()
            .set("v", powerType.v)
    );

    private AttributedEntityAttributeModifier modifier;
    private final float v;

    public LavaVisionPowerType(float v) {
        this.v = v;
    }

    @Override
    public @NotNull PowerConfiguration<?> getConfig() {
        return PowerTypes.LAVA_VISION;
    }

    @Override
    public void onInit() {
        this.modifier = new AttributedEntityAttributeModifier(
            ApoliAttributes.LAVA_VISIBILITY,
            new AttributeModifier(this.getPower().getId(), v - 1, AttributeModifier.Operation.ADD_VALUE)
        );
    }

    @Override
    public void onAdded() {
        addTemporaryModifiers(getHolder());
    }

    @Override
    public void onRemoved() {
        removeModifiers(getHolder());
    }

    @Override
    public List<AttributedEntityAttributeModifier> attributedModifiers() {
        return ObjectArrayList.of(modifier);
    }

    @Override
    public boolean shouldUpdateHealth() {
        return false;
    }

}
