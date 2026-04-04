package io.github.apace100.apoli.mixin.internal;

import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import org.jetbrains.annotations.ApiStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

/**
 * @author Ampflower
 **/
@ApiStatus.Internal
@Mixin(AttributeSupplier.class)
public interface DefaultAttributeContainerAccessor {
    @Accessor
    Map<?, AttributeInstance> getInstances();
}
