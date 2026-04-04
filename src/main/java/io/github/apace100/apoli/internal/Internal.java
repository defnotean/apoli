package io.github.apace100.apoli.internal;

import io.github.apace100.apoli.access.EntityLinkedType;
import io.github.apace100.apoli.access.OwnableAttributeContainer;
import io.github.apace100.apoli.mixin.internal.DefaultAttributeContainerAccessor;
import io.github.apace100.apoli.mixin.internal.DefaultAttributeRegistryAccessor;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.core.registries.BuiltInRegistries;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collection;
import net.minecraft.core.registries.Registries;

/**
 * @author Ampflower
 **/
@ApiStatus.Internal
public final class Internal {

    /**
     * Cleans up the global state of any stray world references when called.
     * <p>
     * Note: Some entries cleaned use ThreadLocal and may not need to be cleaned by the server,
     * as the server discards its thread when it exists.
     * */
    public static void globalStateCleanup() {
        for (final EntityType<?> entityType : BuiltInRegistries.ENTITY_TYPE) {
            if (entityType instanceof EntityLinkedType entityLinkedType) {
                entityLinkedType.apoli$setEntity(null);
            }
        }

        for (final AttributeSupplier container : DefaultAttributeRegistryAccessor.apoli$getRegistry().values()) {
            clearOwnableAttribute(container);

            if (container instanceof DefaultAttributeContainerAccessor accessor) {
                clearOwnableAttributes(accessor.getInstances().values());
            }
        }
    }

    private static void clearOwnableAttributes(Collection<?> ownableAttributeContainers) {
        for (final var entry : ownableAttributeContainers) {
            clearOwnableAttribute(entry);
        }
    }

    private static void clearOwnableAttribute(Object ownableAttribute) {
        if (ownableAttribute instanceof OwnableAttributeContainer ownableAttributeContainer) {
            ownableAttributeContainer.apoli$setOwner(null);
        }
    }
}
