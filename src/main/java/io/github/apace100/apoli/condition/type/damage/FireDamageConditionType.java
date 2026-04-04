package io.github.apace100.apoli.condition.type.damage;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.DamageConditionTypes;
import net.minecraft.tags.DamageTypeTags;
import org.jetbrains.annotations.NotNull;

@Deprecated(forRemoval = true)
public class FireDamageConditionType extends InTagDamageConditionType {

	public FireDamageConditionType() {
		super(DamageTypeTags.IS_FIRE);
	}

	@Override
	public @NotNull ConditionConfiguration<?> getConfig() {
		return DamageConditionTypes.FIRE;
	}

}
