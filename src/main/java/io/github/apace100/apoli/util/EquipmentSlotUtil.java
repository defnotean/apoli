package io.github.apace100.apoli.util;

import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.EquipmentSlot;

import java.util.Optional;

public final class EquipmentSlotUtil {

    public static Optional<EquipmentSlot> fromAttributeModifierSlot(EquipmentSlotGroup modifierSlot) {
        return switch (modifierSlot) {
            case MAINHAND ->
                Optional.of(EquipmentSlot.MAINHAND);
            case OFFHAND ->
                Optional.of(EquipmentSlot.OFFHAND);
            case FEET ->
                Optional.of(EquipmentSlot.FEET);
            case LEGS ->
                Optional.of(EquipmentSlot.LEGS);
            case CHEST ->
                Optional.of(EquipmentSlot.CHEST);
            case HEAD ->
                Optional.of(EquipmentSlot.HEAD);
            case BODY ->
                Optional.of(EquipmentSlot.BODY);
            default ->
                Optional.empty();
        };
    }

}
