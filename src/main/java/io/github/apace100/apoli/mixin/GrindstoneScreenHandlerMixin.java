package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.apace100.apoli.access.PowerModifiedGrindstone;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.type.ModifyGrindstonePowerType;
import io.github.apace100.apoli.util.InventoryUtil;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.Container;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.GrindstoneMenu;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.core.BlockPos;
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
import java.util.List;
import java.util.stream.Collectors;

@Mixin(GrindstoneMenu.class)
public abstract class GrindstoneScreenHandlerMixin extends AbstractContainerMenu implements PowerModifiedGrindstone {

    // MC 26.1: field renamed from 'input' to 'repairSlots'
    @Shadow
    @Final
    Container repairSlots;

    // MC 26.1: field renamed from 'result' to 'resultSlots'
    @Shadow
    @Final
    private Container resultSlots;

    // MC 26.1: field renamed from 'INPUT_1_ID' to 'INPUT_SLOT'
    @Shadow
    @Final
    public static int INPUT_SLOT;

    // MC 26.1: field renamed from 'INPUT_2_ID' to 'ADDITIONAL_SLOT'
    @Shadow
    @Final
    public static int ADDITIONAL_SLOT;

    // MC 26.1: field renamed from 'OUTPUT_ID' to 'RESULT_SLOT'
    @Shadow
    @Final
    public static int RESULT_SLOT;

    // MC 26.1: field renamed from 'context' to 'access'
    @Shadow
    @Final
    private ContainerLevelAccess access;

    @Unique
    private Player apoli$cachedPlayer;

    @Unique
    private List<ModifyGrindstonePowerType> apoli$appliedPowers;

    private GrindstoneScreenHandlerMixin(@Nullable MenuType<?> type, int syncId) {
        super(type, syncId);
    }

    // MC 26.1: constructor takes ContainerLevelAccess, not AbstractContainerMenuContext
    @Inject(method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/inventory/ContainerLevelAccess;)V", at = @At("RETURN"))
    private void cachePlayer(int syncId, Inventory playerInventory, ContainerLevelAccess access, CallbackInfo ci) {
        apoli$cachedPlayer = playerInventory.player;
    }

    // MC 26.1: method renamed from 'updateResult' to 'createResult'
    @Inject(method = "createResult", at = @At("RETURN"))
    private void modifyResult(CallbackInfo ci) {

        ItemStack topStack = repairSlots.getItem(INPUT_SLOT);
        ItemStack bottomStack = repairSlots.getItem(ADDITIONAL_SLOT);

        SlotAccess outputStackRef = InventoryUtil.createStackReference(resultSlots.getItem(0));
        this.apoli$appliedPowers = PowerHolderComponent.getPowerTypes(apoli$cachedPlayer, ModifyGrindstonePowerType.class)
            .stream()
            .filter(mgp -> mgp.doesApply(topStack, bottomStack, outputStackRef.get(), apoli$getPos()))
            .peek(mgp -> mgp.setOutput(topStack, bottomStack, outputStackRef))
            .collect(Collectors.toCollection(LinkedList::new));

        resultSlots.setItem(0, outputStackRef.get());
        this.broadcastChanges();

    }

    @ModifyVariable(method = "quickMoveStack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;copy()Lnet/minecraft/world/item/ItemStack;"), ordinal = 1)
    private ItemStack performAfterGrindstoneActionsQuickMove(ItemStack original, Player player, int slotIndex, @Local Slot slot) {

        List<ModifyGrindstonePowerType> applyingPowers = this.apoli$getAppliedPowers();
        SlotAccess stackReference = InventoryUtil.createStackReference(original);

        if (slotIndex != RESULT_SLOT || applyingPowers == null || applyingPowers.isEmpty()) {
            return original;
        }

        ItemStack copy = original.copy();
        applyingPowers.forEach(mgpt -> mgpt.executeActions(this.apoli$getPos(), stackReference));

        if (stackReference.get().isEmpty()) {
            this.getSlot(slotIndex).onTake(player, copy);
        }

        return stackReference.get();

    }

    @Override
    public List<ModifyGrindstonePowerType> apoli$getAppliedPowers() {
        return apoli$appliedPowers;
    }

    @Override
    public Player apoli$getPlayer() {
        return apoli$cachedPlayer;
    }

    @Nullable
    @Override
    public BlockPos apoli$getPos() {
        return this.access.evaluate((world, pos) -> pos, null);
    }

}
