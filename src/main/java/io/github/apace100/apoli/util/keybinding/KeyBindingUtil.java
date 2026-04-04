package io.github.apace100.apoli.util.keybinding;

import io.github.apace100.apoli.mixin.KeyBindingAccessor;
import io.github.apace100.apoli.util.StringAlias;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;

import java.util.Optional;
import java.util.function.Predicate;

@Environment(EnvType.CLIENT)
public class KeyBindingUtil {

    public static final StringAlias ALIASES = new StringAlias();

    /**
     *  Get the localized name of the keybind from the specified ID. If no such keybind exists or if the keybind is
     *  not bound to any key, use the specified ID instead.
     *
     *  @param translationKey   The translation key of the keybind to get its localized bound key name of.
     *  @return                 Either a {@linkplain Component text} that is localized, or a {@linkplain net.minecraft.text.TranslatableContents translatable text}
     *                              that contains the specified translation key.
     */
    public static MutableComponent getLocalizedName(String translationKey) {
        return getKeyBinding(translationKey)
            .filter(Predicate.not(KeyMapping::isUnbound))
            .map(KeyMapping::getBoundKeyLocalizedText)
            .map(Component::copy)
            .orElseGet(() -> Component.translatable(translationKey));
    }

    public static Optional<KeyMapping> getKeyBinding(String keyBindingId) {

        keyBindingId = ALIASES.hasAlias(keyBindingId)
            ? ALIASES.resolveAlias(keyBindingId)
            : keyBindingId;

        return Optional.ofNullable(KeyBindingAccessor.getKeysById().get(keyBindingId));

    }

}
