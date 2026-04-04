package io.github.apace100.apoli.access;

import io.github.apace100.apoli.power.type.PowerType;
import net.minecraft.world.inventory.TransientCraftingContainer;

import java.util.Collection;

public interface PowerCraftingInventory extends PowerCraftingObject {

    Collection<? extends PowerType> apoli$getPowerTypes();

    void apoli$setPowerTypes(Collection<? extends PowerType> powerType);

    default TransientCraftingContainer apoli$getInventory() {
        return null;
    }

    default void apoli$setInventory(TransientCraftingContainer inventory) {

    }

}
