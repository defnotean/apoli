package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.condition.EntityCondition;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.IntTag;
import net.minecraft.util.Mth;

import java.util.Optional;

public abstract class VariableIntPowerType extends PowerType {

    private final int min, max, startValue;

    private int currentValue;

    public VariableIntPowerType(int startValue, int min, int max, Optional<EntityCondition> condition) {
        super(condition);
        this.currentValue = startValue;
        this.min = min;
        this.max = max;
        this.startValue = startValue;
    }

    public VariableIntPowerType(int startValue, int min, int max) {
        this(startValue, min, max, Optional.empty());
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    public int getValue() {
        return currentValue;
    }

    public int getStartValue() {
        return startValue;
    }

    public int setValue(int newValue) {
        return currentValue = Mth.clamp(newValue, min, max);
    }

    public int increment() {
        return setValue(getValue() + 1);
    }

    public int decrement() {
        return setValue(getValue() - 1);
    }

    @Override
    public Tag toTag() {
        return IntTag.valueOf(currentValue);
    }

    @Override
    public void fromTag(Tag tag) {
        currentValue = Mth.clamp(((IntTag) tag).intValue(), min, max);
    }

}
