package io.github.apace100.apoli.condition.type.entity;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.context.EntityConditionContext;
import io.github.apace100.apoli.condition.type.EntityConditionType;
import io.github.apace100.apoli.condition.type.EntityConditionTypes;
import io.github.apace100.apoli.util.MiscUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class ExposedToSkyEntityConditionType extends EntityConditionType {

    @Override
    public boolean test(EntityConditionContext context) {

        Entity entity = context.entity();
        Level world = entity.level();

        return world.canSeeSky(BlockPos.containing(MiscUtil.getPoseDependentEyePos(entity)))
            || world.canSeeSky(entity.getBlockPos());

    }

    @Override
    public @NotNull ConditionConfiguration<?> getConfig() {
        return EntityConditionTypes.EXPOSED_TO_SKY;
    }

}
