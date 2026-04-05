package io.github.apace100.apoli.condition.type.biome;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.context.BiomeConditionContext;
import io.github.apace100.apoli.condition.type.BiomeConditionType;
import io.github.apace100.apoli.condition.type.BiomeConditionTypes;
import org.jetbrains.annotations.NotNull;

public class HighHumidityBiomeConditionType extends BiomeConditionType {

	@Override
	public boolean test(BiomeConditionContext context) {
		return context.biomeEntry().value().hasPrecipitation();
	}

	@Override
	public @NotNull ConditionConfiguration<?> getConfig() {
		return BiomeConditionTypes.HIGH_HUMIDITY;
	}

}
