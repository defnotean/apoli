package io.github.apace100.apoli.condition.type.damage;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.DamageConditionTypes;
import net.minecraft.tags.DamageTypeTags;
import org.jetbrains.annotations.NotNull;

@Deprecated
public class UnblockableDamageConditionType extends InTagDamageConditionType {

	public UnblockableDamageConditionType() {
		super(DamageTypeTags.BYPASSES_SHIELD);
	}

	@Override
	public @NotNull ConditionConfiguration<?> getConfig() {
		return DamageConditionTypes.UNBLOCKABLE;
	}

}
