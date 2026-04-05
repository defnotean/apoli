package io.github.apace100.apoli.mixin.internal;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;
import org.jetbrains.annotations.ApiStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

/**
 * Proxy to propagate
 *
 * @author Ampflower
 **/
@ApiStatus.Internal
@Mixin(DefaultAttributes.class)
public interface DefaultAttributeRegistryAccessor {

    @Accessor("SUPPLIERS")
    static Map<EntityType<? extends LivingEntity>, AttributeSupplier> apoli$getRegistry() {
        throw new AssertionError();
    }
}
