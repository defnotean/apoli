package io.github.apace100.apoli.action.type.entity;

import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.context.EntityActionContext;
import io.github.apace100.apoli.action.type.EntityActionType;
import io.github.apace100.apoli.action.type.EntityActionTypes;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.util.Space;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.world.entity.Entity;
import org.apache.commons.lang3.function.TriConsumer;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

public class AddVelocityEntityActionType extends EntityActionType {

    public static final TypedDataObjectFactory<AddVelocityEntityActionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("x", SerializableDataTypes.FLOAT, 0F)
            .add("y", SerializableDataTypes.FLOAT, 0F)
            .add("z", SerializableDataTypes.FLOAT, 0F)
            .addFunctionedDefault("velocity", ApoliDataTypes.VECTOR_3_FLOAT, data -> new Vector3f(data.getFloat("x"), data.getFloat("y"), data.getFloat("z")))
            .add("space", ApoliDataTypes.SPACE, Space.WORLD)
            .add("set", SerializableDataTypes.BOOLEAN, false),
        data -> new AddVelocityEntityActionType(
            data.get("velocity"),
            data.get("space"),
            data.get("set")
        ),
        (actionType, serializableData) -> serializableData.instance()
            .set("velocity", actionType.velocity)
            .set("space", actionType.space)
            .set("set", actionType.set)
    );

    private final Vector3f velocity;
    private final Space space;

    private final boolean set;

    public AddVelocityEntityActionType(Vector3f velocity, Space space, boolean set) {
        this.velocity = velocity;
        this.space = space;
        this.set = set;
    }

    @Override
    public void accept(EntityActionContext context) {

        Entity entity = context.entity();

        Vector3f velocityCopy = new Vector3f(velocity);
        TriConsumer<Float, Float, Float> method = set
            ? entity::setDeltaMovement
            : entity::push;

        space.toGlobal(velocityCopy, entity);
        method.accept(velocityCopy.x(), velocityCopy.y(), velocityCopy.z());

        entity.hasImpulse = true;

    }

    @Override
    public @NotNull ActionConfiguration<?> getConfig() {
        return EntityActionTypes.ADD_VELOCITY;
    }

}
