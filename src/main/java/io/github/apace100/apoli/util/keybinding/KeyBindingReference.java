package io.github.apace100.apoli.util.keybinding;

import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.util.MiscUtil;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.KeyMapping;

import java.util.Objects;
import java.util.Optional;

public record KeyBindingReference(String id, boolean continuous) {

	public static final KeyBindingReference NONE = new KeyBindingReference("none");

	public static final TypedDataObjectFactory<KeyBindingReference> DATA_FACTORY = TypedDataObjectFactory.simple(
		new SerializableData()
			.add("key", SerializableDataTypes.STRING, null)
			.addFunctionedDefault("id", SerializableDataTypes.STRING, data -> data.get("key"))
			.add("continuous", SerializableDataTypes.BOOLEAN, false)
			.validate(MiscUtil.validateAllFieldsPresent("id")),
		data -> new KeyBindingReference(
			data.get("id"),
			data.get("continuous")
		),
		(keyBindingReference, serializableData) -> serializableData.instance()
			.set("id", keyBindingReference.id())
			.set("continuous", keyBindingReference.continuous())
	);

	public KeyBindingReference(String id) {
		this(id, false);
	}

	@Override
	public boolean equals(final Object obj) {

		if (this == obj) {
			return true;
		}

		else if (obj instanceof KeyBindingReference that) {
			return this.id().equals(that.id())
				&& this.continuous() == that.continuous();
		}

		else {
			return false;
		}

	}

	@Override
	public int hashCode() {
		return Objects.hash(this.id(), this.continuous());
	}

	@Environment(EnvType.CLIENT)
	public Optional<KeyMapping> asKeyBinding() {
		return KeyBindingUtil.getKeyBinding(id());
	}

}
