package io.github.apace100.apoli.condition.type.entity;

import io.github.apace100.apoli.access.BlockCollisionsAccess;
import io.github.apace100.apoli.condition.BlockCondition;
import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.context.EntityConditionContext;
import io.github.apace100.apoli.condition.type.EntityConditionType;
import io.github.apace100.apoli.condition.type.EntityConditionTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.BlockCollisions;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class BlockCollisionEntityConditionType extends EntityConditionType {

    public static final TypedDataObjectFactory<BlockCollisionEntityConditionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("block_condition", BlockCondition.DATA_TYPE.optional(), Optional.empty())
            .add("offset_x", SerializableDataTypes.DOUBLE, 0.0)
            .add("offset_y", SerializableDataTypes.DOUBLE, 0.0)
            .add("offset_z", SerializableDataTypes.DOUBLE, 0.0)
            .addFunctionedDefault("offset", SerializableDataTypes.VECTOR, data -> new Vec3(data.get("offset_x"), data.get("offset_y"), data.get("offset_z"))),
        data -> new BlockCollisionEntityConditionType(
            data.get("block_condition"),
            data.get("offset")
        ),
        (conditionType, serializableData) -> serializableData.instance()
            .set("block_condition", conditionType.blockCondition)
            .set("offset", conditionType.offset)
    );

    private final Optional<BlockCondition> blockCondition;
    private final Vec3 offset;

    public BlockCollisionEntityConditionType(Optional<BlockCondition> blockCondition, Vec3 offset) {
        this.blockCondition = blockCondition;
        this.offset = offset;
    }

    @Override
    public boolean test(EntityConditionContext context) {

        Entity entity = context.entity();

        AABB boundingBox = entity.getBoundingBox().offset(offset);
        Level world = entity.level();

        BlockCollisions<BlockPos> spliterator = new BlockCollisions<>(world, entity, boundingBox, false, (pos, shape) -> pos);
        ((BlockCollisionsAccess) spliterator).apoli$setGetOriginalShapes(true);

        while (spliterator.hasNext()) {

            BlockPos pos = spliterator.next();

            if (blockCondition.map(condition -> condition.test(world, pos)).orElse(true)) {
                return true;
            }

        }

        return false;

    }

    @Override
    public @NotNull ConditionConfiguration<?> getConfig() {
        return EntityConditionTypes.BLOCK_COLLISION;
    }

}
