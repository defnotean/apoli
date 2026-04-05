package io.github.apace100.apoli.screen.toast;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastManager;

@Environment(EnvType.CLIENT)
public interface PositionAwareToast extends Toast {

    Toast.Visibility draw(int x, int y, GuiGraphicsExtractor context, ToastManager manager, long startTime);

    @Override
    default Visibility draw(GuiGraphicsExtractor context, ToastManager manager, long startTime) {
        return Visibility.HIDE;
    }

}
