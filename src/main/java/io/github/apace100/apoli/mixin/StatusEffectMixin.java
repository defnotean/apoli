package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.type.EffectImmunityPowerType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.core.registries.BuiltInRegistries;
import org.spongepowered.asm.mixin.Mixin;
import net.minecraft.core.registries.Registries;

@Mixin(MobEffect.class)
public abstract class StatusEffectMixin {

    @WrapMethod(method = "applyInstantEffect")
    private void apoli$instantEffectImmunity(Entity source, Entity attacker, LivingEntity target, int amplifier, double proximity, Operation<Void> original) {

        if (!PowerHolderComponent.hasPowerType(target, EffectImmunityPowerType.class, p -> p.doesApply(BuiltInRegistries.MOB_EFFECT.getEntry((MobEffect) (Object) this)))) {
            original.call(source, attacker, target, amplifier, proximity);
        }

    }

}
