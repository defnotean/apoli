package io.github.apace100.apoli.condition.context;

import io.github.apace100.apoli.util.context.ConditionContext;
import net.minecraft.core.Holder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;

public record BiomeConditionContext(BlockPos pos, Holder<Biome> biomeEntry) implements ConditionContext {

}
