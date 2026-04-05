package io.github.apace100.apoli.mixin;

import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TickableTexture;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Map;
import java.util.Set;

@Mixin(TextureManager.class)
public interface TextureManagerAccessor {

    // MC 26.1: field renamed from 'resourceContainer' to 'resourceManager'
    @Accessor("resourceManager")
    ResourceManager getResourceContainer();

    // MC 26.1: field renamed from 'textures' to 'byPath'
    @Accessor("byPath")
    Map<Identifier, AbstractTexture> getTextures();

    // MC 26.1: field renamed from 'tickListeners' to 'tickableTextures'
    @Accessor("tickableTextures")
    Set<TickableTexture> getTickListeners();

    // MC 26.1: method renamed from 'closeTexture' to 'safeClose'
    @Invoker("safeClose")
    void callCloseTexture(Identifier id, AbstractTexture texture);

}
