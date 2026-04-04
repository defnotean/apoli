package io.github.apace100.apoli.mixin;

import net.minecraft.world.entity.ItemEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.UUID;

@Mixin(ItemEntity.class)
public interface ItemEntityAccessor {

    @Nullable
    @Accessor
    UUID getThrowerUuid();

}
