package io.github.apace100.apoli.action.type.entity;

import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.context.EntityActionContext;
import io.github.apace100.apoli.action.type.EntityActionType;
import io.github.apace100.apoli.action.type.EntityActionTypes;
import io.github.apace100.apoli.condition.BiEntityCondition;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class SpawnParticlesEntityActionType extends EntityActionType {

    public static final TypedDataObjectFactory<SpawnParticlesEntityActionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("bientity_condition", BiEntityCondition.DATA_TYPE.optional(), Optional.empty())
            .add("particle", SerializableDataTypes.PARTICLE_EFFECT_OR_TYPE)
            .add("offset_x", SerializableDataTypes.DOUBLE, 0.0D)
            .add("offset_y", SerializableDataTypes.DOUBLE, 0.5D)
            .add("offset_z", SerializableDataTypes.DOUBLE, 0.0D)
            .addFunctionedDefault("offset", SerializableDataTypes.VECTOR, data -> new Vec3(data.getDouble("offset_x"), data.getDouble("offset_y"), data.getDouble("offset_z")))
            .add("spread", SerializableDataTypes.VECTOR, new Vec3(0.5D, 0.5D, 0.5D))
            .add("force", SerializableDataTypes.BOOLEAN, false)
            .add("speed", SerializableDataTypes.FLOAT, 0.0F)
            .add("count", SerializableDataTypes.INT, 1),
        data -> new SpawnParticlesEntityActionType(
            data.get("bientity_condition"),
            data.get("particle"),
            data.get("offset"),
            data.get("spread"),
            data.get("force"),
            data.get("speed"),
            data.get("count")
        ),
        (actionType, serializableData) -> serializableData.instance()
            .set("bientity_condition", actionType.biEntityCondition)
            .set("particle", actionType.particle)
            .set("offset", actionType.offset)
            .set("spread", actionType.spread)
            .set("force", actionType.force)
            .set("speed", actionType.speed)
            .set("count", actionType.count)
    );

    private final Optional<BiEntityCondition> biEntityCondition;
    private final ParticleOptions particle;

    private final Vec3 offset;
    private final Vec3 spread;

    private final boolean force;
    private final float speed;
    private final int count;

    public SpawnParticlesEntityActionType(Optional<BiEntityCondition> biEntityCondition, ParticleOptions particle, Vec3 offset, Vec3 spread, boolean force, float speed, int count) {
        this.biEntityCondition = biEntityCondition;
        this.particle = particle;
        this.offset = offset;
        this.spread = spread;
        this.force = force;
        this.speed = speed;
        this.count = count;
    }

    @Override
    public void accept(EntityActionContext context) {

        Entity entity = context.entity();
        Vec3 pos = entity.position().add(context.offset()).add(offset);

        if (!(entity.level() instanceof ServerLevel serverWorld)) {
            return;
        }

        Vec3 delta = spread.multiply(entity.getWidth(), entity.getHeight(), entity.getWidth());
        serverWorld.players()
            .stream()
            .filter(player -> biEntityCondition.map(condition -> condition.test(entity, player)).orElse(true))
            .forEach(player -> serverWorld.spawnParticles(player, particle, force, pos.getX(), pos.getY(), pos.getZ(), count, delta.getX(), delta.getY(), delta.getZ(), speed));

    }

    @Override
    public @NotNull ActionConfiguration<?> getConfig() {
        return EntityActionTypes.SPAWN_PARTICLES;
    }

}
