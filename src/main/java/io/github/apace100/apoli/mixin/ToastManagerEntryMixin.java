package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import io.github.apace100.apoli.screen.toast.PositionAwareToast;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastManager;
import com.mojang.blaze3d.vertex.PoseStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ToastManager.ToastInstance.class)
public abstract class ToastManagerEntryMixin {

    @WrapOperation(method = "draw", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(FFF)V"))
    private void apoli$test(PoseStack matrices, float x, float y, float z, Operation<Void> original, @Share("toastX") LocalIntRef sharedToastX, @Share("toastY") LocalIntRef sharedToastY) {

        sharedToastX.set((int) x);
        sharedToastY.set((int) y);

        original.call(matrices, x, y, z);

    }

    @WrapOperation(method = "draw", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/toasts/Toast;draw(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/gui/components/toasts/ToastManager;J)Lnet/minecraft/client/gui/components/toasts/Toast$Visibility;"))
    private Toast.Visibility apoli$drawAsPositionAwareToast(Toast toast, GuiGraphicsExtractor context, ToastManager toastManager, long startTime, Operation<Toast.Visibility> original, @Share("toastX") LocalIntRef sharedToastX, @Share("toastY") LocalIntRef sharedToastY) {
        return toast instanceof PositionAwareToast positionAwareToast
            ? positionAwareToast.draw(sharedToastX.get(), sharedToastY.get(), context, toastManager, startTime)
            : original.call(toast, context, toastManager, startTime);
    }

}
