package io.github.apace100.apoli.screen.toast;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;

@Environment(EnvType.CLIENT)
public interface PositionAwareToast extends Toast {

    Toast.Visibility draw(int x, int y, GuiRenderer context, ToastManager manager, long startTime);

    @Override
    default Visibility draw(GuiRenderer context, ToastManager manager, long startTime) {
        return Visibility.HIDE;
    }

}
