package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.github.apace100.apoli.access.OwnableAttributeContainer;
import io.github.apace100.apoli.access.OwnableAttributeInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AttributeSupplier.class)
public abstract class DefaultAttributeContainerMixin implements OwnableAttributeContainer {

    @Unique
    private final ThreadLocal<Entity> apoli$owner = new ThreadLocal<>();

    @Override
    @Nullable
    public Entity apoli$getOwner() {
        return apoli$owner.get();
    }

    @Override
    public void apoli$setOwner(Entity owner) {
        this.apoli$owner.set(owner);
    }

    @ModifyExpressionValue(method = "getValue", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/attribute/AttributeSupplier;require(Lnet/minecraft/core/Holder;)Lnet/minecraft/world/entity/attribute/AttributeInstance;"))
    private AttributeInstance apoli$setAttributeInstanceOwner(AttributeInstance original) {

        if (original instanceof OwnableAttributeInstance ownableAttributeInstance) {
            ownableAttributeInstance.apoli$setOwner(this.apoli$getOwner());
        }

        return original;

    }

}
