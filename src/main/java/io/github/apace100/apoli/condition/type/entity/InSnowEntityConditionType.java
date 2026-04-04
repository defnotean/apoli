package io.github.apace100.apoli.condition.type.entity;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.context.EntityConditionContext;
import io.github.apace100.apoli.condition.type.EntityConditionType;
import io.github.apace100.apoli.condition.type.EntityConditionTypes;
import io.github.apace100.apoli.util.MiscUtil;
import io.github.apace100.apoli.util.WorldUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.NotNull;

public class InSnowEntityConditionType extends EntityConditionType {

	@Override
	public boolean test(EntityConditionContext context) {
		Entity entity = context.entity();
		return WorldUtil.inSnow(entity.level(), BlockPos.ofFloored(MiscUtil.getPoseDependentEyePos(entity)), entity.blockPosition());
	}

	@Override
	public @NotNull ConditionConfiguration<?> getConfig() {
		return EntityConditionTypes.IN_SNOW;
	}

}
