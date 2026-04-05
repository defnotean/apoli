package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.apace100.apoli.access.PowerCraftingInventory;
import io.github.apace100.apoli.access.ScreenHandlerUsabilityOverride;
import io.github.apace100.apoli.power.type.ModifyCraftingPowerType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.LinkedList;

@Mixin(CraftingMenu.class)
public abstract class CraftingScreenHandlerMixin extends RecipeBookMenu implements ScreenHandlerUsabilityOverride {

    @Shadow
    @Final
    private CraftingContainer input;

    @Shadow @Final private Player player;
    @Unique
    private boolean apoli$canUse = false;

    @Override
    public boolean apoli$canUse() {
        return this.apoli$canUse;
    }

    @Override
    public void apoli$canUse(boolean canUse) {
        this.apoli$canUse = canUse;
    }

    private CraftingScreenHandlerMixin(MenuType screenHandlerType, int i) {
        super(screenHandlerType, i);
    }

    @ModifyExpressionValue(method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/inventory/AbstractContainerMenuContext;)V", at = @At(value = "NEW", target = "(Lnet/minecraft/world/inventory/AbstractContainerMenu;II)Lnet/minecraft/world/inventory/TransientCraftingContainer;"))
    private TransientCraftingContainer apoli$cachePlayerToCraftingInventory(TransientCraftingContainer original, int syncId, Inventory playerInventory) {

        if (original instanceof PowerCraftingInventory pci) {
            pci.apoli$setPlayer(playerInventory.player);
        }

        return original;

    }

    @Inject(method = "updateResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/crafting/RecipeManager;getFirstMatch(Lnet/minecraft/world/item/crafting/RecipeType;Lnet/minecraft/world/item/crafting/RecipeInput;Lnet/minecraft/world/Level;Lnet/minecraft/world/item/crafting/RecipeHolder;)Ljava/util/Optional;"))
    private static void apoli$clearPowerCraftingInventory(AbstractContainerMenu handler, Level world, Player player, CraftingContainer craftingInventory, ResultContainer resultInventory, @Nullable RecipeHolder<CraftingRecipe> recipe, CallbackInfo ci) {

        if (craftingInventory instanceof PowerCraftingInventory pci) {
            pci.apoli$setPowerTypes(new LinkedList<>());
        }

    }

    @ModifyReturnValue(method = "canUse", at = @At("RETURN"))
    private boolean apoli$allowUsingViaPower(boolean original, Player playerEntity) {
        return original || this.apoli$canUse();
    }

    @ModifyVariable(method = "quickMove", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/CraftingMenu;insertItem(Lnet/minecraft/world/item/ItemStack;IIZ)Z", ordinal = 0), ordinal = 1)
    private ItemStack apoli$modifyResultStackOnQuickMove(ItemStack original, Player player, int slotId, @Local Slot slot) {
        return ModifyCraftingPowerType.executeAfterCraftingAction(player, input, slot, original);
    }

}
