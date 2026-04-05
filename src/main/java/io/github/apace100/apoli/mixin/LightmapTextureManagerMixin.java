package io.github.apace100.apoli.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.Lightmap;
import org.spongepowered.asm.mixin.Mixin;

// TODO: MC 26.1 completely rewrote the Lightmap class. It no longer has a 'client' field or 'update' method.
// NightVisionPowerType lightmap modification needs to be reimplemented using the new
// LightmapRenderStateExtractor or the static render(LightmapRenderState) pipeline.
@Mixin(Lightmap.class)
@Environment(EnvType.CLIENT)
public abstract class LightmapTextureManagerMixin implements AutoCloseable {

}
