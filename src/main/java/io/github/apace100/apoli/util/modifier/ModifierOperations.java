package io.github.apace100.apoli.util.modifier;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.registry.ApoliRegistries;
import net.minecraft.core.Registry;

import java.util.Locale;
import net.minecraft.core.registries.Registries;

public final class ModifierOperations {

    public static void registerAll() {
        for(ModifierOperation operation : ModifierOperation.values()) {
            Registry.register(ApoliRegistries.MODIFIER_OPERATION,
                Apoli.identifier(operation.toString().toLowerCase(Locale.ROOT)),
                operation);
        }
    }
}
