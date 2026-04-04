package io.github.apace100.apoli.condition.type.damage;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.condition.context.DamageConditionContext;
import io.github.apace100.apoli.condition.type.DamageConditionType;
import io.github.apace100.apoli.condition.type.DamageConditionTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class AttackerDamageConditionType extends DamageConditionType {

    public static final TypedDataObjectFactory<AttackerDamageConditionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("entity_condition", EntityCondition.DATA_TYPE.optional(), Optional.empty()),
        data -> new AttackerDamageConditionType(
            data.get("entity_condition")
        ),
        (conditionType, serializableData) -> serializableData.instance()
            .set("entity_condition", conditionType.entityCondition)
    );

    private final Optional<EntityCondition> entityCondition;

    public AttackerDamageConditionType(Optional<EntityCondition> entityCondition) {
        this.entityCondition = entityCondition;
    }

    @Override
    public boolean test(DamageConditionContext context) {
        Entity attacker = context.source().getEntity();
        return attacker != null && entityCondition
            .map(condition -> condition.test(attacker))
            .orElse(true);
    }

    @Override
    public @NotNull ConditionConfiguration<?> getConfig() {
        return DamageConditionTypes.ATTACKER;
    }

}
