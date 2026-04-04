package io.github.apace100.apoli.mixin;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(TextureAtlas.class)
public interface SpriteAtlasTextureAccessor {

    @Accessor
    TextureAtlasSprite getMissingSprite();

}
