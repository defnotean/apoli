package io.github.apace100.apoli.data;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.container.ContainerType;
import io.github.apace100.apoli.data.container.PresetContainerType;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.util.IdentifierAlias;
import net.minecraft.core.Registry;
import net.minecraft.world.inventory.DispenserMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.HopperMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.resources.Identifier;

import java.util.Objects;
import java.util.stream.Collectors;
import net.minecraft.core.registries.Registries;

public class ApoliContainerTypes {

	public static final IdentifierAlias ALIASES = new IdentifierAlias();
	public static final SerializableDataType<ContainerType> REGISTRY_DATA_TYPE = SerializableDataType.registry(ApoliRegistries.CONTAINER_TYPE, Apoli.MODID, ALIASES, (containerTypes, id) -> "Container type \"" + id + "\" is undefined! Expected to be any of " + containerTypes.keySet().stream().map(Identifier::toString).collect(Collectors.joining(", ")));

	//	Presets for generic container screen handlers
	public static final PresetContainerType GENERIC_3X3 = register("generic_3x3", new PresetContainerType(3, 3, (inventory, columns, rows) -> (syncId, playerInventory, player) -> new DispenserMenu(syncId, playerInventory, inventory)));
	public static final PresetContainerType GENERIC_9X1	= register("generic_9x1", new PresetContainerType(9, 1, (inventory, columns, rows) -> (syncId, playerInventory, player) -> new ChestMenu(MenuType.GENERIC_9x1, syncId, playerInventory, inventory, rows)));
	public static final PresetContainerType GENERIC_9X2 = register("generic_9x2", new PresetContainerType(9, 2, (inventory, columns, rows) -> (syncId, playerInventory, player) -> new ChestMenu(MenuType.GENERIC_9x2, syncId, playerInventory, inventory, rows)));
	public static final PresetContainerType GENERIC_9X3 = register("generic_9x3", new PresetContainerType(9, 3, (inventory, columns, rows) -> (syncId, playerInventory, player) -> new ChestMenu(MenuType.GENERIC_9x3, syncId, playerInventory, inventory, rows)));
	public static final PresetContainerType GENERIC_9X4 = register("generic_9x4", new PresetContainerType(9, 4, (inventory, columns, rows) -> (syncId, playerInventory, player) -> new ChestMenu(MenuType.GENERIC_9x4, syncId, playerInventory, inventory, rows)));
	public static final PresetContainerType GENERIC_9X5 = register("generic_9x5", new PresetContainerType(9, 5, (inventory, columns, rows) -> (syncId, playerInventory, player) -> new ChestMenu(MenuType.GENERIC_9x5, syncId, playerInventory, inventory, rows)));
	public static final PresetContainerType GENERIC_9X6 = register("generic_9x6", new PresetContainerType(9, 6, (inventory, columns, rows) -> (syncId, playerInventory, player) -> new ChestMenu(MenuType.GENERIC_9x6, syncId, playerInventory, inventory, rows)));

	//	Presets for other container screen handlers
	public static final PresetContainerType HOPPER = register("hopper", new PresetContainerType(5, 1, (inventory, columns, rows) -> (syncId, playerInventory, player) -> new HopperMenu(syncId, playerInventory, inventory)));

	public static void register() {
		ALIASES.addPathAlias("double_chest", getPath(GENERIC_9X6));
		ALIASES.addPathAlias("chest", getPath(GENERIC_9X3));
		ALIASES.addPathAlias("dropper", getPath(GENERIC_3X3));
		ALIASES.addPathAlias("dispenser", getPath(GENERIC_3X3));
	}

	private static String getPath(ContainerType containerType) {
		return Objects.requireNonNull(ApoliRegistries.CONTAINER_TYPE.getKey(containerType)).getPath();
	}

	public static <T extends ContainerType> T register(String name, T containerType) {
		return Registry.register(ApoliRegistries.CONTAINER_TYPE, Apoli.identifier(name), containerType);
	}

}
