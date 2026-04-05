package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.apace100.apoli.power.type.ModifyTypeTagPowerType;
import net.minecraft.tags.TagLoader;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;
import java.util.Map;

@Mixin(TagLoader.class)
public abstract class TagGroupLoaderMixin<T> {

    @Shadow
    @Final
    private String directory;

    @ModifyReturnValue(method = "build", at = @At("RETURN"))
    private Map<Identifier, List<T>> apoli$rebuildTagsInTags(Map<Identifier, List<T>> original) {
        ModifyTypeTagPowerType.setTagCache(directory, original);
        return original;
    }

}
