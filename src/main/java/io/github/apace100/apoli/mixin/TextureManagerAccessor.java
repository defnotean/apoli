package io.github.apace100.apoli.mixin;

import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.Tickable;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Map;
import java.util.Set;

@Mixin(TextureManager.class)
public interface TextureManagerAccessor {

    @Accessor
    ResourceManager getResourceContainer();

    @Accessor
    Map<ResourceLocation, AbstractTexture> getTextures();

    @Accessor
    Set<Tickable> getTickListeners();

    @Invoker
    void callCloseTexture(ResourceLocation id, AbstractTexture texture);

}
