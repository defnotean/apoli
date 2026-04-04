package io.github.apace100.apoli.condition.type.entity;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.context.EntityConditionContext;
import io.github.apace100.apoli.condition.type.EntityConditionType;
import io.github.apace100.apoli.condition.type.EntityConditionTypes;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public class CreativeFlyingEntityConditionType extends EntityConditionType {

	@Override
	public boolean test(EntityConditionContext context) {
		return context.entity() instanceof Player player
			&& player.getAbilities().flying;
	}

	@Override
	public @NotNull ConditionConfiguration<?> getConfig() {
		return EntityConditionTypes.CREATIVE_FLYING;
	}

}
