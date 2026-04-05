package io.github.apace100.apoli.power.type;


import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.power.PowerConfiguration;
import io.github.apace100.apoli.util.keybinding.KeyBindingReference;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class ToggleNightVisionPowerType extends NightVisionPowerType implements Active {

    public static final TypedDataObjectFactory<ToggleNightVisionPowerType> DATA_FACTORY = PowerType.createConditionedDataFactory(
        new SerializableData()
            .add("key", ApoliDataTypes.BACKWARDS_COMPATIBLE_KEY, KeyBindingReference.NONE)
            .add("active_by_default", SerializableDataTypes.BOOLEAN, false)
            .add("strength", SerializableDataTypes.FLOAT, 1.0F),
        (data, condition) -> new ToggleNightVisionPowerType(
            data.get("key"),
            data.get("active_by_default"),
            data.get("strength"),
            condition
        ),
        (powerType, serializableData) -> serializableData.instance()
            .set("key", powerType.getKey())
            .set("active_by_default", powerType.activeByDefault)
            .set("strength", powerType.getStrength())
    );

    private final KeyBindingReference key;
    private final boolean activeByDefault;

    private boolean toggled;

    public ToggleNightVisionPowerType(KeyBindingReference key, boolean activeByDefault, float strength, Optional<EntityCondition> condition) {
        super(strength, condition);
        this.key = key;
        this.activeByDefault = activeByDefault;
        this.toggled = activeByDefault;
    }

    @Override
    public @NotNull PowerConfiguration<?> getConfig() {
        return PowerTypes.TOGGLE_NIGHT_VISION;
    }

    @Override
    public void onUse() {
        this.toggled = !this.toggled;
        PowerHolderComponent.syncPower(getHolder(), getPower());
    }

    public boolean isActive() {
        return this.toggled && super.isActive();
    }

    @Override
    public Tag toTag() {
        return ByteTag.valueOf(toggled);
    }

    @Override
    public void fromTag(Tag tag) {

        if (tag instanceof ByteTag nbtByte) {
            toggled = nbtByte.byteValue() > 0;
        }

    }

    @Override
    public KeyBindingReference getKey() {
        return key;
    }

}
