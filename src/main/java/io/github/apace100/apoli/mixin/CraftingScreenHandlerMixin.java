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
import net.minecraft.world.inventory.AbstractCraftingMenu;
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
public abstract class CraftingScreenHandlerMixin extends AbstractCraftingMenu implements ScreenHandlerUsabilityOverride {

    // MC 26.1: 'input' field is now 'craftSlots' on parent AbstractCraftingMenu,
    // accessible directly since we extend it. @Shadow removed.

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

    private CraftingScreenHandlerMixin(MenuType screenHandlerType, int i, int w, int h) {
        super(screenHandlerType, i, w, h);
    }

    // MC 26.1: constructor takes ContainerLevelAccess, not AbstractContainerMenuContext
    @ModifyExpressionValue(method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/inventory/ContainerLevelAccess;)V", at = @At(value = "NEW", target = "(Lnet/minecraft/world/inventory/AbstractContainerMenu;II)Lnet/minecraft/world/inventory/TransientCraftingContainer;"))
    private TransientCraftingContainer apoli$cachePlayerToCraftingInventory(TransientCraftingContainer original, int syncId, Inventory playerInventory) {

        if (original instanceof PowerCraftingInventory pci) {
            pci.apoli$setPlayer(playerInventory.player);
        }

        return original;

    }

    // MC 26.1: method renamed from 'updateResult' to 'slotChangedCraftingGrid'
    // Signature: (AbstractContainerMenu, ServerLevel, Player, CraftingContainer, ResultContainer, RecipeHolder)
    @Inject(method = "slotChangedCraftingGrid", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/crafting/RecipeManager;getRecipeFor(Lnet/minecraft/world/item/crafting/RecipeType;Lnet/minecraft/world/item/crafting/RecipeInput;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/crafting/RecipeHolder;)Ljava/util/Optional;"))
    private static void apoli$clearPowerCraftingInventory(AbstractContainerMenu handler, net.minecraft.server.level.ServerLevel world, Player player, CraftingContainer craftingInventory, ResultContainer resultInventory, @Nullable RecipeHolder<CraftingRecipe> recipe, CallbackInfo ci) {

        if (craftingInventory instanceof PowerCraftingInventory pci) {
            pci.apoli$setPowerTypes(new LinkedList<>());
        }

    }

    // TODO: MC 26.1 renamed canUse -> stillValid
    @ModifyReturnValue(method = "stillValid", at = @At("RETURN"))
    private boolean apoli$allowUsingViaPower(boolean original, Player playerEntity) {
        return original || this.apoli$canUse();
    }

    // TODO: MC 26.1 renamed quickMove -> quickMoveStack, insertItem -> moveItemStackTo
    @ModifyVariable(method = "quickMoveStack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/CraftingMenu;moveItemStackTo(Lnet/minecraft/world/item/ItemStack;IIZ)Z", ordinal = 0), ordinal = 1)
    private ItemStack apoli$modifyResultStackOnQuickMove(ItemStack original, Player player, int slotId, @Local Slot slot) {
        return ModifyCraftingPowerType.executeAfterCraftingAction(player, craftSlots, slot, original);
    }

}
