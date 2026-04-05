package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.apace100.apoli.power.type.ModifyGrindstonePowerType;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.GrindstoneMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net/minecraft/world/inventory/GrindstoneMenu$2")
public class GrindstoneScreenHandlerTopInputSlotMixin {

    @Unique
    private GrindstoneMenu apoli$grindstoneHandler;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void apoli$cacheGrindstone(GrindstoneMenu grindstoneScreenHandler, Container inventory, int i, int j, int k, CallbackInfo ci) {
        this.apoli$grindstoneHandler = grindstoneScreenHandler;
    }

    @ModifyReturnValue(method = "mayPlace", at = @At("RETURN"))
    private boolean apoli$allowStackInTopSlotViaPower(boolean original, ItemStack stack) {
        return original
            || ModifyGrindstonePowerType.allowsInTopSlot(apoli$grindstoneHandler, stack);
    }

}
