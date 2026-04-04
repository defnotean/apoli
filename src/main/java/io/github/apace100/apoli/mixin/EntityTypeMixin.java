package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.apace100.apoli.access.EntityLinkedType;
import io.github.apace100.apoli.power.type.ModifyTypeTagPowerType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.core.HolderSet;
import net.minecraft.tags.TagKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.lang.ref.WeakReference;

@Mixin(EntityType.class)
public abstract class EntityTypeMixin implements EntityLinkedType {

    @Unique
    private final ThreadLocal<WeakReference<Entity>> apoli$currentEntity = new ThreadLocal<>();

    @Override
    public Entity apoli$getEntity() {
        final WeakReference<Entity> reference = apoli$currentEntity.get();
        if (reference != null) {
            return reference.get();
        }
        return null;
    }

    @Override
    public void apoli$setEntity(Entity entity) {
        this.apoli$currentEntity.set(new WeakReference<>(entity));
    }

    @ModifyReturnValue(method = "isIn(Lnet/minecraft/tags/TagKey;)Z", at = @At("RETURN"))
    private boolean apoli$inTagProxy(boolean original, TagKey<EntityType<?>> tag) {
        return original
            || ModifyTypeTagPowerType.doesApply(this.apoli$getEntity(), tag);
    }

    @ModifyReturnValue(method = "isIn(Lnet/minecraft/core/HolderSet;)Z", at = @At("RETURN"))
    private boolean apoli$inTagEntryListProxy(boolean original, HolderSet<EntityType<?>> entryList) {
        return original
            || ModifyTypeTagPowerType.doesApply(this.apoli$getEntity(), entryList);
    }

}
