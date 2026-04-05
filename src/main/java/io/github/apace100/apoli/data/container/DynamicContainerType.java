package io.github.apace100.apoli.data.container;

import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.util.TextAlignment;
import io.github.apace100.calio.data.CompoundSerializableDataType;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.Identifier;

/**
 * A fully dynamic container type that allows specifying arbitrary column and row counts.
 * Columns must be 1–9 and rows 1–6. For exact multiples of 9, uses vanilla ChestMenu.
 * For other sizes, renders a custom inventory screen.
 */
public record DynamicContainerType(TextAlignment titleAlignment, Identifier texture, int columns, int rows) implements ContainerType {

    public static final TypedDataObjectFactory<DynamicContainerType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("title_alignment", ApoliDataTypes.TEXT_ALIGNMENT, TextAlignment.CENTER)
            .add("texture", SerializableDataTypes.IDENTIFIER)
            .add("columns", SerializableDataTypes.POSITIVE_INT)
            .add("rows", SerializableDataTypes.POSITIVE_INT),
        data -> new DynamicContainerType(
            data.get("title_alignment"),
            data.get("texture"),
            data.get("columns"),
            data.get("rows")
        ),
        (containerType, serializableData) -> serializableData.instance()
            .set("title_alignment", containerType.titleAlignment())
            .set("texture", containerType.texture())
            .set("columns", containerType.columns())
            .set("rows", containerType.rows())
    );

    public static final CompoundSerializableDataType<DynamicContainerType> DATA_TYPE = DATA_FACTORY.getDataType();

    @Override
    public MenuProvider create(Container inventory) {
        int size = columns * rows;
        // For standard 9-wide chests (1-6 rows), use vanilla ChestMenu — vanilla-compatible and renderable
        if (columns == 9 && rows >= 1 && rows <= 6) {
            MenuType<ChestMenu> menuType = switch (rows) {
                case 1 -> MenuType.GENERIC_9x1;
                case 2 -> MenuType.GENERIC_9x2;
                case 3 -> MenuType.GENERIC_9x3;
                case 4 -> MenuType.GENERIC_9x4;
                case 5 -> MenuType.GENERIC_9x5;
                default -> MenuType.GENERIC_9x6;
            };
            return new MenuProvider() {
                @Override
                public Component getDisplayName() { return Component.empty(); }
                @Override
                public AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player) {
                    return new ChestMenu(menuType, syncId, playerInventory, inventory, rows);
                }
            };
        }
        // For non-standard sizes, clamp to the nearest valid chest size and use dynamic slots
        return new MenuProvider() {
            @Override
            public Component getDisplayName() { return Component.empty(); }
            @Override
            public AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player) {
                return new DynamicMenu(syncId, playerInventory, inventory, columns, rows);
            }
        };
    }

    /**
     * A custom container menu supporting arbitrary slot grids up to 9×6.
     * Renders as a vertically flexible chest-like screen.
     */
    public static class DynamicMenu extends AbstractContainerMenu {

        private final int columns;
        private final int rows;

        public DynamicMenu(int syncId, net.minecraft.world.entity.player.Inventory playerInventory,
                           Container inventory, int columns, int rows) {
            super(resolveMenuType(columns, rows), syncId);
            this.columns = columns;
            this.rows = rows;

            checkContainerSize(inventory, columns * rows);

            // Add container slots
            for (int row = 0; row < rows; row++) {
                for (int col = 0; col < columns; col++) {
                    this.addSlot(new Slot(inventory, col + row * columns, 8 + col * 18, 18 + row * 18));
                }
            }

            // Add player inventory slots (3 rows)
            int playerStartY = 18 + rows * 18 + 13;
            for (int row = 0; row < 3; row++) {
                for (int col = 0; col < 9; col++) {
                    this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, playerStartY + row * 18));
                }
            }

            // Add player hotbar slots
            int hotbarY = playerStartY + 58;
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col, 8 + col * 18, hotbarY));
            }
        }

        private static MenuType<?> resolveMenuType(int columns, int rows) {
            // Use the closest standard chest menu type for the row count
            return switch (Math.min(rows, 6)) {
                case 1 -> MenuType.GENERIC_9x1;
                case 2 -> MenuType.GENERIC_9x2;
                case 3 -> MenuType.GENERIC_9x3;
                case 4 -> MenuType.GENERIC_9x4;
                case 5 -> MenuType.GENERIC_9x5;
                default -> MenuType.GENERIC_9x6;
            };
        }

        @Override
        public ItemStack quickMoveStack(Player player, int index) {
            ItemStack itemStack = ItemStack.EMPTY;
            Slot slot = this.slots.get(index);
            int containerSize = columns * rows;

            if (slot.hasItem()) {
                ItemStack slotStack = slot.getItem();
                itemStack = slotStack.copy();

                if (index < containerSize) {
                    if (!this.moveItemStackTo(slotStack, containerSize, this.slots.size(), true)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!this.moveItemStackTo(slotStack, 0, containerSize, false)) {
                    return ItemStack.EMPTY;
                }

                if (slotStack.isEmpty()) {
                    slot.setByPlayer(ItemStack.EMPTY);
                } else {
                    slot.setChanged();
                }
            }

            return itemStack;
        }

        @Override
        public boolean stillValid(Player player) {
            return true;
        }

    }

}
