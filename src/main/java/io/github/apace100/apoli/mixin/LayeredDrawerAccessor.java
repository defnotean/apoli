package io.github.apace100.apoli.mixin;

import net.minecraft.client.gui.LayeredDraw;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(LayeredDraw.class)
public interface LayeredDrawerAccessor {

    @Accessor
    List<LayeredDraw.Layer> getLayers();

}
