package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.apace100.apoli.power.type.ModifyTypeTagPowerType;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagLoader;
import net.minecraft.server.packs.resources.DependencyTracker;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Mixin(TagLoader.class)
public abstract class TagGroupLoaderMixin<T> {

    @Shadow
    @Final
    private String dataType;

    @Inject(method = "buildGroup", at = @At("RETURN"))
    private void apoli$rebuildTagsInTags(Map<ResourceLocation, List<TagLoader.TrackedEntry>> tags, CallbackInfoReturnable<Map<ResourceLocation, Collection<T>>> cir, @Local TagEntry.ValueGetter<T> valueGetter, @Local DependencyTracker<ResourceLocation, TagLoader.TagDependencies> dependencyTracker) {
        ModifyTypeTagPowerType.setTagCache(dataType, valueGetter, dependencyTracker);
    }

}
