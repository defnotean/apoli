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

    @Accessor
    ResourceManager getResourceContainer();

    @Accessor
    Map<Identifier, AbstractTexture> getTextures();

    @Accessor
    Set<TickableTexture> getTickListeners();

    @Invoker
    void callCloseTexture(Identifier id, AbstractTexture texture);

}
