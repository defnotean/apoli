package io.github.apace100.apoli.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.Power;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

public class GainedPowerCriterion extends SimpleCriterionTrigger<GainedPowerCriterion.Conditions> {

    public static final GainedPowerCriterion INSTANCE = new GainedPowerCriterion();
    public static final ResourceLocation ID = Apoli.identifier("gained_power");

    @Override
    public Codec<Conditions> getConditionsCodec() {
        return Conditions.CODEC;
    }

    public void trigger(ServerPlayer player, Power power) {
        this.trigger(player, conditions -> conditions.matches(power));
    }

    public record Conditions(Optional<ContextAwarePredicate> player, ResourceLocation powerId) implements SimpleCriterionTrigger.Conditions {

        public static final Codec<Conditions> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            EntityPredicate.LOOT_CONTEXT_PREDICATE_CODEC.optionalFieldOf("player").forGetter(Conditions::player),
            ResourceLocation.CODEC.fieldOf("power").forGetter(Conditions::powerId)
        ).apply(instance, Conditions::new));

        @Override
        public Optional<ContextAwarePredicate> player() {
            return player;
        }

        public boolean matches(Power power) {
            return power.getId().equals(powerId);
        }

    }

}
