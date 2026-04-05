package io.github.apace100.apoli.data.container;

import io.github.apace100.apoli.util.TextAlignment;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuConstructor;
import org.jetbrains.annotations.NotNull;

public record PresetContainerType(int columns, int rows, @NotNull Factory factory) implements ContainerType {

	@Override
	public TextAlignment titleAlignment() {
		return TextAlignment.NONE;
	}

	@Override
	public MenuProvider create(Container inventory) {
		MenuConstructor constructor = factory().create(inventory, columns(), rows());
		return new MenuProvider() {
			@Override
			public Component getDisplayName() {
				return Component.empty();
			}

			@Override
			public AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player) {
				return constructor.createMenu(syncId, playerInventory, player);
			}
		};
	}

	@FunctionalInterface
	public interface Factory {
		MenuConstructor create(Container inventory, int columns, int rows);
	}

}
