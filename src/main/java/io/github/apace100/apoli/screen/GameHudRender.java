package io.github.apace100.apoli.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.renderer.RenderTickCounter;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public interface GameHudRender {

    List<GameHudRender> HUD_RENDERS = new ArrayList<>();

    void render(GuiRenderer context, RenderTickCounter renderTickCounter);
}
