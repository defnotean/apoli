package io.github.apace100.apoli.access;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public interface OverlaySpriteHolder {
    TextureAtlasSprite apoli$getSprite(ResourceLocation id);
}
