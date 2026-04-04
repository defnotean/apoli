package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.apace100.apoli.power.type.ModifyGrindstonePowerType;
import net.minecraft.world.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.GrindstoneMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net/minecraft/world/inventory/GrindstoneMenu$3")
public class GrindstoneScreenHandlerBottomInputSlotMixin {

    @Unique
    private GrindstoneMenu apoli$grindstoneHandler;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void apoli$cacheGrindstone(GrindstoneMenu grindstoneScreenHandler, Inventory inventory, int i, int j, int k, CallbackInfo ci) {
        this.apoli$grindstoneHandler = grindstoneScreenHandler;
    }

    @ModifyReturnValue(method = "canInsert", at = @At("RETURN"))
    private boolean apoli$allowPowerStacks(boolean original, ItemStack stack) {
        return original
            || ModifyGrindstonePowerType.allowsInBottomSlot(apoli$grindstoneHandler, stack);
    }

}
