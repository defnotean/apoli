package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.apace100.apoli.access.OwnableAttributeInstance;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.type.ModifyAttributePowerType;
import io.github.apace100.apoli.util.modifier.Modifier;
import io.github.apace100.apoli.util.modifier.ModifierUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.core.Holder;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

@Mixin(AttributeInstance.class)
public abstract class EntityAttributeInstanceMixin implements OwnableAttributeInstance {

    @Shadow
    public abstract Set<AttributeModifier> getModifiers();

    @Shadow
    public abstract double getBaseValue();

    @Shadow public abstract Holder<Attribute> getAttribute();

    @Unique
    @Nullable
    WeakReference<Entity> apoli$owner;

    @Override
    public void apoli$setOwner(Entity owner) {
        apoli$owner = new WeakReference<>(owner);
    }

    @Override
    public Entity apoli$getOwner() {
        if (apoli$owner != null) {
            return apoli$owner.get();
        }
        return null;
    }

    /**
     *  TODO: Optimize this impl. by using a modifier cache, injecting into {@link AttributeInstance#computeValue()}, and calling
     *        {@link AttributeInstance#onUpdate()} if the modifier cache is no longer up-to-date -eggohito
     */
    @SuppressWarnings("JavadocReference")
    @ModifyReturnValue(method = "getValue", at = @At("RETURN"))
    private double apoli$modifyAttribute(double original) {

        List<Modifier> powerModifiers = PowerHolderComponent.getPowerTypes(this.apoli$getOwner(), ModifyAttributePowerType.class)
            .stream()
            .filter(p -> p.getAttribute() == this.getAttribute())
            .flatMap(p -> p.getModifiers().stream())
            .toList();

        if (powerModifiers.isEmpty()) {
            return original;
        }

        List<Modifier> vanillaModifiers = this.getModifiers()
            .stream()
            .map(ModifierUtil::fromAttributeModifier)
            .toList();

        return ModifierUtil.applyModifiers(this.apoli$getOwner(), Stream.concat(powerModifiers.stream(), vanillaModifiers.stream()).toList(), this.getBaseValue());

    }

}
