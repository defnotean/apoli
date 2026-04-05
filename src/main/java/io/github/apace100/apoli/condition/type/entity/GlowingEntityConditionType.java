package io.github.apace100.apoli.condition.type.entity;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.context.EntityConditionContext;
import io.github.apace100.apoli.condition.type.EntityConditionType;
import io.github.apace100.apoli.condition.type.EntityConditionTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

public class GlowingEntityConditionType extends EntityConditionType {

	@Override
	public boolean test(EntityConditionContext context) {
		Entity entity = context.entity();
		return !entity.level().isClientSide()
			? entity.isCurrentlyGlowing()
			: entity.isCurrentlyGlowing();
	}

	@Override
	public @NotNull ConditionConfiguration<?> getConfig() {
		return EntityConditionTypes.GLOWING;
	}

}
