package io.github.apace100.apoli.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LivingEntity.class)
public interface LivingEntityAccessor {
    @Accessor("SPRINTING_SPEED_BOOST")
    static AttributeModifier apoli$getSprintingSpeedBoost() {
        throw new RuntimeException();
    }
}
