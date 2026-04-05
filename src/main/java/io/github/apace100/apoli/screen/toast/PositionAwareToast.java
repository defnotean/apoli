package io.github.apace100.apoli.screen.toast;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastManager;

@Environment(EnvType.CLIENT)
public interface PositionAwareToast extends Toast {

    void extractRenderStatePositionAware(int x, int y, GuiGraphicsExtractor context, Font font, long startTime);

    @Override
    default void extractRenderState(GuiGraphicsExtractor context, Font font, long startTime) {
        // Default fallback; the mixin will intercept and call extractRenderStatePositionAware instead
    }

}
