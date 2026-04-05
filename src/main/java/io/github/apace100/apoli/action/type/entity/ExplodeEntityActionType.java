package io.github.apace100.apoli.action.type.entity;

import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.context.EntityActionContext;
import io.github.apace100.apoli.action.type.EntityActionType;
import io.github.apace100.apoli.action.type.EntityActionTypes;
import io.github.apace100.apoli.condition.BlockCondition;
import io.github.apace100.apoli.condition.context.BlockConditionContext;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.util.MiscUtil;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Explosion;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

public class ExplodeEntityActionType extends EntityActionType {

    public static final TypedDataObjectFactory<ExplodeEntityActionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("destructible", BlockCondition.DATA_TYPE, null)
            .add("indestructible", BlockCondition.DATA_TYPE, null)
            .add("destruction_type", SerializableDataTypes.DESTRUCTION_TYPE, Explosion.BlockInteraction.DESTROY)
            .add("damage_self", SerializableDataTypes.BOOLEAN, true)
            .add("create_fire", SerializableDataTypes.BOOLEAN, false)
            .add("power", SerializableDataTypes.FLOAT)
            .add("indestructible_resistance", SerializableDataTypes.FLOAT, 10.0F),
        data -> new ExplodeEntityActionType(
            data.get("destructible"),
            data.get("indestructible"),
            data.get("destruction_type"),
            data.get("damage_self"),
            data.get("create_fire"),
            data.get("power"),
            data.get("indestructible_resistance")
        ),
        (actionType, serializableData) -> serializableData.instance()
            .set("destructible", actionType.destructibleCondition)
            .set("indestructible", actionType.indestructibleCondition)
            .set("destruction_type", actionType.destructionType)
            .set("damage_self", actionType.damageSelf)
            .set("create_fire", actionType.createFire)
            .set("power", actionType.power)
            .set("indestructible_resistance", actionType.indestructibleResistance)
    );

    private final BlockCondition destructibleCondition;
    private final BlockCondition indestructibleCondition;

    private final Explosion.BlockInteraction destructionType;

    private final boolean damageSelf;
    private final boolean createFire;

    private final float power;
    private final float indestructibleResistance;

    public ExplodeEntityActionType(BlockCondition destructibleCondition, BlockCondition indestructibleCondition, Explosion.BlockInteraction destructionType, boolean damageSelf, boolean createFire, float power, float indestructibleResistance) {
        this.destructibleCondition = destructibleCondition;
        this.indestructibleCondition = indestructibleCondition;
        this.destructionType = destructionType;
        this.damageSelf = damageSelf;
        this.createFire = createFire;
        this.power = power;
        this.indestructibleResistance = indestructibleResistance;
    }

    @Override
    public void accept(EntityActionContext context) {

        Entity entity = context.entity();
        Level world = entity.level();

        if (world.isClientSide()) {
            return;
        }

        Predicate<BlockConditionContext> behaviorCondition = indestructibleCondition;
        if (destructibleCondition != null) {
            behaviorCondition = MiscUtil.combineOr(destructibleCondition.negate(), behaviorCondition);
        }

        MiscUtil.createExplosion(
            entity.level(),
            damageSelf ? null : entity,
            Explosion.getDefaultDamageSource(entity.level(), entity),
            entity.position().x(),
            entity.position().y(),
            entity.position().z(),
            power,
            createFire,
            destructionType,
            MiscUtil.createExplosionDamageCalculator(behaviorCondition, indestructibleResistance)
        );

    }

    @Override
    public @NotNull ActionConfiguration<?> getConfig() {
        return EntityActionTypes.EXPLODE;
    }

}
