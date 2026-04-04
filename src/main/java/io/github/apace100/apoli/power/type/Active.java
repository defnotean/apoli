package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.ApoliClient;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.util.keybinding.KeyBindingReference;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public interface Active {

    KeyBindingReference getKey();

    default boolean canTrigger() {
        return true;
    }

    void onUse();

    @Environment(EnvType.CLIENT)
    static <P extends PowerType & Active> void integrateCallback(Minecraft client) {

        if (client.player == null) {
            return;
        }

        List<PowerType> powerTypes = PowerHolderComponent.getOptional(client.player).orElseThrow().getPowerTypes();
        List<P> triggeredPowerTypes = new LinkedList<>();

        Map<String, Boolean> currentKeybindingStates = new HashMap<>();
        for (PowerType powerType : powerTypes) {

            if (!(powerType instanceof Active activePowerType)) {
                continue;
            }

            KeyBindingReference keyBindingReference = activePowerType.getKey();
            TriState keyPressed = keyBindingReference.asKeyBinding()
                .map(KeyMapping::isPressed)
                .map(TriState::of)
                .orElse(TriState.DEFAULT);

            if (keyPressed == TriState.DEFAULT) {
                continue;
            }

            if (currentKeybindingStates.computeIfAbsent(keyBindingReference.id(), k -> keyPressed.get()) && (keyBindingReference.continuous() || !ApoliClient.lastKeyBindingStates.getOrDefault(keyBindingReference.id(), false))) {
				//noinspection unchecked
				triggeredPowerTypes.add((P) powerType);
            }

        }

        ApoliClient.lastKeyBindingStates.putAll(currentKeybindingStates);
        ApoliClient.performActivePowerTypes(triggeredPowerTypes);

    }

}
