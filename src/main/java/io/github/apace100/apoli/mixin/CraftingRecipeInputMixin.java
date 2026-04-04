package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.access.PowerCraftingInventory;
import io.github.apace100.apoli.power.type.PowerType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.crafting.CraftingInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.Collection;
import java.util.LinkedList;

@Mixin(CraftingInput.class)
public abstract class CraftingRecipeInputMixin implements PowerCraftingInventory {

    @Unique
    private Collection<? extends PowerType> apoli$cachedPowerTypes = new LinkedList<>();

    @Unique
    private Player apoli$cachedPlayer;

    @Unique
    private TransientCraftingContainer apoli$inventory;

    @Override
    public Collection<? extends PowerType> apoli$getPowerTypes() {
        return apoli$cachedPowerTypes;
    }

    @Override
    public void apoli$setPowerTypes(Collection<? extends PowerType> powerType) {

        this.apoli$cachedPowerTypes = powerType;

        if (this.apoli$getInventory() instanceof PowerCraftingInventory pci) {
            pci.apoli$setPowerTypes(this.apoli$getPowerTypes());
        }

    }

    @Override
    public Player apoli$getPlayer() {
        return apoli$cachedPlayer;
    }

    @Override
    public void apoli$setPlayer(Player player) {

        this.apoli$cachedPlayer = player;

        if (this.apoli$getInventory() instanceof PowerCraftingInventory pci) {
            pci.apoli$setPlayer(this.apoli$getPlayer());
        }

    }

    @Override
    public TransientCraftingContainer apoli$getInventory() {
        return apoli$inventory;
    }

    @Override
    public void apoli$setInventory(TransientCraftingContainer inventory) {
        this.apoli$inventory = inventory;
    }

}
