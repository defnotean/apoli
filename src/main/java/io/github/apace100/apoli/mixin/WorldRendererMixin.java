package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.ApoliClient;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.type.PhasingPowerType;
import io.github.apace100.apoli.util.MiscUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.Lightmap;
import net.minecraft.world.entity.Entity;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(LevelRenderer.class)
public abstract class WorldRendererMixin {

    // TODO: MC 26.1 completely restructured LevelRenderer. Both renderSky() and render()
    // no longer exist with these signatures. The rendering pipeline was overhauled with
    // frame graph architecture. These need reimplementing against the new API.
    // @Shadow public abstract void reload();

    // @Inject(method = "renderSky(...)", ...)
    // private void skipSkyRenderingForPhasingBlindness(...) { ... }

    // @Inject(method = "render", ...)
    // private void updateChunksIfRenderChanged(...) { ... }

}
