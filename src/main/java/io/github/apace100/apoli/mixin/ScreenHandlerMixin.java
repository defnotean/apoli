package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.apace100.apoli.access.PowerCraftingInventory;
import io.github.apace100.apoli.access.PowerModifiedGrindstone;
import io.github.apace100.apoli.power.type.ModifyCraftingPowerType;
import io.github.apace100.apoli.power.type.ModifyGrindstonePowerType;
import io.github.apace100.apoli.recipe.ModifiedCraftingRecipe;
import io.github.apace100.apoli.util.InventoryUtil;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.ClickAction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;
import java.util.Optional;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
@Mixin(AbstractContainerMenu.class)
public class ScreenHandlerMixin {

    // TODO: MC 26.1 renamed internalOnSlotClick -> doClick, and ClickAction -> ContainerInput
    @ModifyExpressionValue(method = "doClick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/Slot;tryRemove(IILnet/minecraft/world/entity/player/Player;)Ljava/util/Optional;"))
    private Optional<ItemStack> apoli$performAfterCraftingActions(Optional<ItemStack> original, int slotIndex, int button, net.minecraft.world.inventory.ContainerInput actionType, Player player, @Local Slot slot) {

        if ((AbstractContainerMenu) (Object) this instanceof PowerModifiedGrindstone pmg && original.isPresent() && slotIndex == 2) {

            List<ModifyGrindstonePowerType> applyingPowers = pmg.apoli$getAppliedPowers();
            if (applyingPowers == null || applyingPowers.isEmpty()) {
                return original;
            }

            SlotAccess stackReference = InventoryUtil.createStackReference(original.get());
            applyingPowers.forEach(mgpt -> mgpt.executeActions(pmg.apoli$getPos(), stackReference));

            return Optional.of(stackReference.get());

        }

        else if (original.isPresent() && slot instanceof ResultSlot resultSlot) {

            if (!(((CraftingResultSlotAccessor) resultSlot).getInput() instanceof TransientCraftingContainer craftingInventory)) {
                return original;
            }

            if (!(craftingInventory instanceof PowerCraftingInventory pci)) {
                return original;
            }

            List<ModifyCraftingPowerType> modifyCraftingPowers = pci.apoli$getPowerTypes()
                .stream()
                .filter(ModifyCraftingPowerType.class::isInstance)
                .map(ModifyCraftingPowerType.class::cast)
                .toList();

            if (modifyCraftingPowers.isEmpty()) {
                return original;
            }

            modifyCraftingPowers.forEach(mcpt -> mcpt.executeActions(ModifiedCraftingRecipe.getBlockFromInventory(craftingInventory)));
            SlotAccess stackReference = InventoryUtil.createStackReference(original.get());

            modifyCraftingPowers.forEach(mcpt -> mcpt.applyAfterCraftingItemAction(stackReference));
            return Optional.of(stackReference.get());

        }

        return original;

    }

}
