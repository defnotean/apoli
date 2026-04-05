package io.github.apace100.apoli.condition.type.item;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.context.ItemConditionContext;
import io.github.apace100.apoli.condition.type.ItemConditionType;
import io.github.apace100.apoli.condition.type.ItemConditionTypes;
import net.minecraft.core.component.DataComponents;
import org.jetbrains.annotations.NotNull;

public class FireResistantItemConditionType extends ItemConditionType {

	@Override
	public boolean test(ItemConditionContext context) {
		return context.stack().has(DataComponents.DAMAGE_RESISTANT);
	}

	@Override
	public @NotNull ConditionConfiguration<?> getConfig() {
		return ItemConditionTypes.FIRE_RESISTANT;
	}

}
