package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.apace100.apoli.access.PowerCraftingInventory;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.type.ModifyCraftingPowerType;
import io.github.apace100.apoli.power.type.RestrictArmorPowerType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.RecipeInputInventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(InventoryMenu.class)
public abstract class PlayerScreenHandlerMixin {

    @Shadow
    @Final
    private RecipeInputInventory craftingInput;

    @ModifyExpressionValue(method = "<init>", at = @At(value = "NEW", target = "(Lnet/minecraft/world/inventory/AbstractContainerMenu;II)Lnet/minecraft/world/inventory/TransientCraftingContainer;"))
    private TransientCraftingContainer apoli$cachePlayerToCraftingInventory(TransientCraftingContainer original, Inventory playerInventory) {

        if (original instanceof PowerCraftingInventory pci) {
            pci.apoli$setPlayer(playerInventory.player);
        }

        return original;

    }

    @ModifyExpressionValue(method = "quickMove", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/Slot;hasItem()Z", ordinal = 0), slice = @Slice(from = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/EquipmentSlot$Type;HUMANOID_ARMOR:Lnet/minecraft/world/entity/EquipmentSlot$Type;")))
    private boolean apoli$disallowQuickMovingRestrictedWearables(boolean original, Player player, @Local(ordinal = 1) ItemStack stackToInsert, @Local EquipmentSlot slot) {
        return original
            || PowerHolderComponent.hasPowerType(player, RestrictArmorPowerType.class, p -> p.doesRestrict(stackToInsert, slot));
    }

    @ModifyVariable(method = "quickMove", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/InventoryMenu;insertItem(Lnet/minecraft/world/item/ItemStack;IIZ)Z", ordinal = 0), ordinal = 1)
    private ItemStack apoli$modifyResultStackOnQuickMove(ItemStack original, Player player, int slotId, @Local Slot slot) {
        return ModifyCraftingPowerType.executeAfterCraftingAction(player, craftingInput, slot, original);
    }

}
