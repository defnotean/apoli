package io.github.apace100.apoli.mixin;

import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(KeyMapping.class)
public interface KeyBindingAccessor {

    @Accessor("KEYS_BY_ID")
    static Map<String, KeyMapping> getKeysById() {
        throw new AssertionError();
    }

}
