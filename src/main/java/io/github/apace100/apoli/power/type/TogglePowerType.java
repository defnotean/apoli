package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerConfiguration;
import io.github.apace100.apoli.util.keybinding.KeyBindingReference;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class TogglePowerType extends PowerType implements Active {

    public static final TypedDataObjectFactory<TogglePowerType> DATA_FACTORY = PowerType.createConditionedDataFactory(
        new SerializableData()
            .add("key", ApoliDataTypes.BACKWARDS_COMPATIBLE_KEY, KeyBindingReference.NONE)
            .add("retain_state", SerializableDataTypes.BOOLEAN, true)
            .add("active_by_default", SerializableDataTypes.BOOLEAN, true),
        (data, condition) -> new TogglePowerType(
            data.get("key"),
            data.get("retain_state"),
            data.get("active_by_default"),
            condition
        ),
        (powerType, serializableData) -> serializableData.instance()
            .set("key", powerType.getKey())
            .set("retain_state", powerType.shouldRetainState)
            .set("active_by_default", powerType.activeByDefault)
    );

    private final KeyBindingReference key;

    private final boolean activeByDefault;
    private final boolean shouldRetainState;

    private boolean toggled;

    public TogglePowerType(KeyBindingReference key, boolean retainState, boolean activeByDefault, Optional<EntityCondition> condition) {
        super(condition);
        this.key = key;
        this.activeByDefault = activeByDefault;
        this.shouldRetainState = retainState;
        this.toggled = activeByDefault;
    }

    @Override
    public @NotNull PowerConfiguration<?> getConfig() {
        return PowerTypes.TOGGLE;
    }

    @Override
    public boolean shouldTick() {
        return !shouldRetainState && this.condition.isPresent();
    }

    @Override
    public boolean shouldTickWhenInactive() {
        return true;
    }

    @Override
    public void serverTick() {

        if (!super.isActive() && this.toggled) {
            this.toggled = false;
            PowerHolderComponent.syncPower(getHolder(), getPower());
        }

    }

    @Override
    public void onUse() {

        Entity holder = getHolder();
        Power power = getPower();

        if (!holder.level().isClientSide()) {
            this.toggled = !this.toggled;
            PowerHolderComponent.syncPower(holder, power);
        }

    }

    @Override
    public boolean canTrigger() {
        return super.isActive();
    }

    public boolean isActive() {
        return this.toggled && super.isActive();
    }

    @Override
    public Tag toTag() {
        return ByteTag.of(toggled);
    }

    @Override
    public void fromTag(Tag tag) {

        if (tag instanceof ByteTag nbtByte) {
            this.toggled = nbtByte.byteValue() > 0;
        }

    }

    @Override
    public KeyBindingReference getKey() {
        return key;
    }

}
