package io.github.apace100.apoli.action.type.entity;

import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.BiEntityAction;
import io.github.apace100.apoli.action.context.BiEntityActionContext;
import io.github.apace100.apoli.action.context.EntityActionContext;
import io.github.apace100.apoli.action.type.EntityActionType;
import io.github.apace100.apoli.action.type.EntityActionTypes;
import io.github.apace100.apoli.condition.BiEntityCondition;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.util.Shape;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;

public class AreaOfEffectEntityActionType extends EntityActionType {

    public static final TypedDataObjectFactory<AreaOfEffectEntityActionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("bientity_action", BiEntityAction.DATA_TYPE)
            .add("bientity_condition", BiEntityCondition.DATA_TYPE.optional(), Optional.empty())
            .add("shape", SerializableDataType.enumValue(Shape.class), Shape.CUBE)
            .add("radius", SerializableDataTypes.POSITIVE_DOUBLE, 16.0D)
            .add("include_actor", SerializableDataTypes.BOOLEAN, false),
        data -> new AreaOfEffectEntityActionType(
            data.get("bientity_action"),
            data.get("bientity_condition"),
            data.get("shape"),
            data.get("radius"),
            data.get("include_actor")
        ),
        (actionType, serializableData) -> serializableData.instance()
            .set("bientity_action", actionType.biEntityAction)
            .set("bientity_condition", actionType.biEntityCondition)
            .set("shape", actionType.shape)
            .set("radius", actionType.radius)
            .set("include_actor", actionType.includeActor)
    );

    private final BiEntityAction biEntityAction;
    private final Optional<BiEntityCondition> biEntityCondition;

    private final Shape shape;

    private final double radius;
    private final boolean includeActor;

    public AreaOfEffectEntityActionType(BiEntityAction biEntityAction, Optional<BiEntityCondition> biEntityCondition, Shape shape, double radius, boolean includeActor) {
        this.biEntityAction = biEntityAction;
        this.biEntityCondition = biEntityCondition;
        this.shape = shape;
        this.radius = radius;
        this.includeActor = includeActor;
    }

    @Override
    public void accept(EntityActionContext context) {

        Entity actor = context.entity();
        Vec3 lerpedPos = actor.getLerpedPos(1.0F);

        shape.getEntities(actor.level(), lerpedPos.lerp(actor.position().add(context.offset()), 1.0), radius)
            .stream()
            .filter(target -> includeActor || !Objects.equals(actor, target))
            .map(target -> new BiEntityActionContext(actor, target))
            .filter(biEntityActionContext -> biEntityCondition.map(condition -> condition.test(biEntityActionContext.forCondition())).orElse(true))
            .forEach(biEntityAction);

    }

    @Override
    public @NotNull ActionConfiguration<?> getConfig() {
        return EntityActionTypes.AREA_OF_EFFECT;
    }

}
