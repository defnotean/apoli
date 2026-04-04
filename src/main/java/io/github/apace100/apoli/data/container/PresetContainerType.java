package io.github.apace100.apoli.data.container;

import io.github.apace100.apoli.util.TextAlignment;
import net.minecraft.world.Inventory;
import net.minecraft.world.MenuProvider;
import org.jetbrains.annotations.NotNull;

public record PresetContainerType(int columns, int rows, @NotNull Factory factory) implements ContainerType {

	@Override
	public TextAlignment titleAlignment() {
		return TextAlignment.NONE;
	}

	@Override
	public MenuProvider create(Inventory inventory) {
		return factory().create(inventory, columns(), rows());
	}

	@FunctionalInterface
	public interface Factory {
		MenuProvider create(Inventory inventory, int columns, int rows);
	}

}
