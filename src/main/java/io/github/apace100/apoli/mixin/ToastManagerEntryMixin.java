package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import io.github.apace100.apoli.screen.toast.PositionAwareToast;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastManager;
import org.joml.Matrix3x2fStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ToastManager.ToastInstance.class)
public abstract class ToastManagerEntryMixin {

    @WrapOperation(method = "draw", at = @At(value = "INVOKE", target = "Lorg/joml/Matrix3x2fStack;translate(FF)Lorg/joml/Matrix3x2f;"))
    private org.joml.Matrix3x2f apoli$capturePosition(Matrix3x2fStack stack, float x, float y, Operation<org.joml.Matrix3x2f> original, @Share("toastX") LocalIntRef sharedToastX, @Share("toastY") LocalIntRef sharedToastY) {

        sharedToastX.set((int) x);
        sharedToastY.set((int) y);

        return original.call(stack, x, y);

    }

    @WrapOperation(method = "draw", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/toasts/Toast;extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/gui/Font;J)V"))
    private void apoli$drawAsPositionAwareToast(Toast toast, GuiGraphicsExtractor context, Font font, long startTime, Operation<Void> original, @Share("toastX") LocalIntRef sharedToastX, @Share("toastY") LocalIntRef sharedToastY) {
        if (toast instanceof PositionAwareToast positionAwareToast) {
            positionAwareToast.extractRenderStatePositionAware(sharedToastX.get(), sharedToastY.get(), context, font, startTime);
        } else {
            original.call(toast, context, font, startTime);
        }
    }

}
