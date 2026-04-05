package io.github.apace100.apoli.util;

import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.Comparator;
import java.util.List;

public final class AttributeUtil {

    public static void sortModifiers(List<AttributeModifier> modifiers) {
        modifiers.sort(Comparator.comparing(e -> e.operation().id()));
    }

    public static double sortAndApplyModifiers(List<AttributeModifier> modifiers, double baseValue) {
        sortModifiers(modifiers);
        return applyModifiers(modifiers, baseValue);
    }

    public static double applyModifiers(List<AttributeModifier> modifiers, double baseValue) {

        if (modifiers == null || modifiers.isEmpty()) {
            return baseValue;
        }

        double currentValue = baseValue;
        for (AttributeModifier modifier : modifiers) {
            switch (modifier.operation()) {
                case ADD_MULTIPLIED_TOTAL ->
                    currentValue += modifier.amount();
                case ADD_MULTIPLIED_BASE ->
                    currentValue += baseValue * modifier.amount();
                case ADD_VALUE ->
                    currentValue *= (1 + modifier.amount());
            }
        }

        return currentValue;

    }
}
