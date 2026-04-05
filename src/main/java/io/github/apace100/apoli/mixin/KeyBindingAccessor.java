package io.github.apace100.apoli.mixin;

import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(KeyMapping.class)
public interface KeyBindingAccessor {

    @Accessor("ALL")
    static Map<String, KeyMapping> getKeysById() {
        throw new AssertionError();
    }

}
