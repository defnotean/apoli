package io.github.apace100.apoli.data.container;

import io.github.apace100.apoli.util.TextAlignment;
import net.minecraft.world.Inventory;
import net.minecraft.world.MenuProvider;
import org.jetbrains.annotations.Range;

public interface ContainerType {

	TextAlignment titleAlignment();

	MenuProvider create(Inventory inventory);

	default int size() {
		return columns() * rows();
	}

	@Range(from = 1, to = Integer.MAX_VALUE)
	int columns();

	@Range(from = 1, to = Integer.MAX_VALUE)
	int rows();

}
