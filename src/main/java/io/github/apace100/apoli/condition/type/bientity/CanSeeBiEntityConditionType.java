package io.github.apace100.apoli.condition.type.bientity;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.context.BiEntityConditionContext;
import io.github.apace100.apoli.condition.type.BiEntityConditionType;
import io.github.apace100.apoli.condition.type.BiEntityConditionTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.util.requirement.BiEntityRequirement;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.ClipContext;
import org.jetbrains.annotations.NotNull;

public class CanSeeBiEntityConditionType extends BiEntityConditionType {

    public static final TypedDataObjectFactory<CanSeeBiEntityConditionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("shape_type", SerializableDataTypes.SHAPE_TYPE, ClipContext.Block.VISUAL)
            .add("fluid_handling", SerializableDataTypes.FLUID_HANDLING, ClipContext.Fluid.NONE),
        data -> new CanSeeBiEntityConditionType(
            data.get("shape_type"),
            data.get("fluid_handling")
        ),
        (conditionType, serializableData) -> serializableData.instance()
            .set("shape_type", conditionType.shapeType)
            .set("fluid_handling", conditionType.fluidHandling)
    );

    private final ClipContext.Block shapeType;
    private final ClipContext.Fluid fluidHandling;

    public CanSeeBiEntityConditionType(ClipContext.Block shapeType, ClipContext.Fluid fluidHandling) {
        this.shapeType = shapeType;
        this.fluidHandling = fluidHandling;
    }

    @Override
    public boolean test(BiEntityConditionContext context) {

        Entity actor = context.actor();
        Entity target = context.target();

        if (actor.level() != target.level()) {
            return false;
        }

        Vec3 actorEyePos = actor.getEyePosition();
        Vec3 targetEyePos = target.getEyePosition();

        ClipContext ClipContext = new ClipContext(actorEyePos, targetEyePos, shapeType, fluidHandling, actor);
        return actor.level().raycast(ClipContext).getType() == HitResult.Type.MISS;

    }

    @Override
    public BiEntityRequirement getRequirement() {
        return BiEntityRequirement.BOTH;
    }

    @Override
    public @NotNull ConditionConfiguration<?> getConfig() {
        return BiEntityConditionTypes.CAN_SEE;
    }

}
