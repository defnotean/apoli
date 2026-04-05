package io.github.apace100.apoli.action.type.entity;

import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.BiEntityAction;
import io.github.apace100.apoli.action.EntityAction;
import io.github.apace100.apoli.action.context.BiEntityActionContext;
import io.github.apace100.apoli.action.context.EntityActionContext;
import io.github.apace100.apoli.action.type.EntityActionType;
import io.github.apace100.apoli.action.type.EntityActionTypes;
import io.github.apace100.apoli.condition.BiEntityCondition;
import io.github.apace100.apoli.condition.context.BiEntityConditionContext;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class PassengerActionEntityActionType extends EntityActionType {

    public static final TypedDataObjectFactory<PassengerActionEntityActionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("action", EntityAction.DATA_TYPE.optional(), Optional.empty())
            .addFunctionedDefault("entity_action", EntityAction.DATA_TYPE.optional(), data -> data.get("action"))
            .add("bientity_action", BiEntityAction.DATA_TYPE.optional(), Optional.empty())
            .add("bientity_condition", BiEntityCondition.DATA_TYPE.optional(), Optional.empty())
            .add("recursive", SerializableDataTypes.BOOLEAN, false),
        data -> new PassengerActionEntityActionType(
            data.get("entity_action"),
            data.get("bientity_action"),
            data.get("bientity_condition"),
            data.get("recursive")
        ),
        (actionType, serializableData) -> serializableData.instance()
            .set("entity_action", actionType.entityAction)
            .set("bientity_action", actionType.biEntityAction)
            .set("bientity_condition", actionType.biEntityCondition)
            .set("recursive", actionType.recursive)
    );

    private final Optional<EntityAction> entityAction;
    private final Optional<BiEntityAction> biEntityAction;

    private final Optional<BiEntityCondition> biEntityCondition;
    private final boolean recursive;

    public PassengerActionEntityActionType(Optional<EntityAction> entityAction, Optional<BiEntityAction> biEntityAction, Optional<BiEntityCondition> biEntityCondition, boolean recursive) {
        this.entityAction = entityAction;
        this.biEntityAction = biEntityAction;
        this.biEntityCondition = biEntityCondition;
        this.recursive = recursive;
    }

    @Override
    public void accept(EntityActionContext context) {

        Entity entity = context.entity();

        if (!entity.isVehicle()) {
            return;
        }

        Iterable<Entity> passengers = recursive
            ? entity.getPassengersAndSelf()
            : entity.getPassengers();

        for (Entity passenger : passengers) {

            BiEntityActionContext actionContext = new BiEntityActionContext(entity, passenger);
            BiEntityConditionContext conditionContext = actionContext.forCondition();

            if (biEntityCondition.map(condition -> condition.test(conditionContext)).orElse(true)) {
                entityAction.ifPresent(action -> action.execute(passenger));
                biEntityAction.ifPresent(action -> action.accept(actionContext));
            }

        }

    }

    @Override
    public @NotNull ActionConfiguration<?> getConfig() {
        return EntityActionTypes.PASSENGER;
    }

}
