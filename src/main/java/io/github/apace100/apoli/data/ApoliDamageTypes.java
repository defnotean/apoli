package io.github.apace100.apoli.data;

import io.github.apace100.apoli.Apoli;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;

public interface ApoliDamageTypes {
    ResourceKey<DamageType> SYNC_DAMAGE_SOURCE = ResourceKey.create(Registries.DAMAGE_TYPE, Apoli.identifier("sync_damage_source"));
}
