package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.condition.BiEntityCondition;
import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.power.PowerConfiguration;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class ParticlePowerType extends PowerType {

    public static final TypedDataObjectFactory<ParticlePowerType> DATA_FACTORY = createConditionedDataFactory(
        new SerializableData()
            .add("bientity_condition", BiEntityCondition.DATA_TYPE.optional(), Optional.empty())
            .add("particle", SerializableDataTypes.PARTICLE_EFFECT_OR_TYPE)
            .add("offset_x", SerializableDataTypes.DOUBLE, 0.0D)
            .add("offset_y", SerializableDataTypes.DOUBLE, 0.0D)
            .add("offset_z", SerializableDataTypes.DOUBLE, 0.0D)
            .addFunctionedDefault("offset", SerializableDataTypes.VECTOR, data -> new Vec3(data.get("offset_x"), data.get("offset_y"), data.get("offset_z")))
            .add("spread", SerializableDataTypes.VECTOR, new Vec3(0.5, 0.5, 0.5))
            .add("frequency", SerializableDataTypes.POSITIVE_INT)
            .add("count", SerializableDataTypes.INT, 1)
            .add("speed", SerializableDataTypes.FLOAT, 0.0F)
            .add("visible_in_first_person", SerializableDataTypes.BOOLEAN, false)
            .add("visible_while_invisible", SerializableDataTypes.BOOLEAN, false)
            .add("force", SerializableDataTypes.BOOLEAN, false),
        (data, condition) -> new ParticlePowerType(
            data.get("bientity_condition"),
            data.get("particle"),
            data.get("offset"),
            data.get("spread"),
            data.get("frequency"),
            data.get("count"),
            data.get("speed"),
            data.get("visible_in_first_person"),
            data.get("visible_while_invisible"),
            data.get("force"),
            condition
        ),
        (powerType, serializableData) -> serializableData.instance()
            .set("bientity_condition", powerType.biEntityCondition)
            .set("particle", powerType.getParticle())
            .set("offset", powerType.getOffset())
            .set("spread", powerType.getSpread())
            .set("frequency", powerType.getFrequency())
            .set("count", powerType.getCount())
            .set("speed", powerType.getSpeed())
            .set("visible_in_first_person", powerType.isVisibleInFirstPerson())
            .set("visible_while_invisible", powerType.isVisibleWhileInvisible())
            .set("force", powerType.shouldForce())
    );

    private final Optional<BiEntityCondition> biEntityCondition;
    private final ParticleOptions particleEffect;

    private final Vec3 spread;
    private final Vec3 offset;

    private final int frequency;
    private final int count;

    private final float speed;

    private final boolean visibleInFirstPerson;
    private final boolean visibleWhileInvisible;
    private final boolean force;

    public ParticlePowerType(Optional<BiEntityCondition> biEntityCondition, ParticleOptions particleEffect, Vec3 offset, Vec3 spread, int frequency, int count, float speed, boolean visibleInFirstPerson, boolean visibleWhileInvisible, boolean force, Optional<EntityCondition> condition) {
        super(condition);
        this.biEntityCondition = biEntityCondition;
        this.particleEffect = particleEffect;
        this.spread = spread;
        this.offset = offset;
        this.frequency = frequency;
        this.count = Math.max(0, count);
        this.speed = speed;
        this.visibleInFirstPerson = visibleInFirstPerson;
        this.visibleWhileInvisible = visibleWhileInvisible;
        this.force = force;
    }

    @Override
    public @NotNull PowerConfiguration<?> getConfig() {
        return PowerTypes.PARTICLE;
    }

    public boolean doesApply(Player viewer, boolean inFirstPerson) {
        LivingEntity holder = getHolder();
        return (!holder.isInvisibleTo(viewer) || this.isVisibleWhileInvisible())
            && (holder != viewer || (!inFirstPerson || this.isVisibleInFirstPerson()))
            && (viewer.blockPosition().isWithinDistance(holder.position(), this.shouldForce() ? 512 : 32))
            && (holder.tickCount % this.getFrequency() == 0)
            && biEntityCondition.map(condition -> condition.test(viewer, holder)).orElse(true);
    }

    public ParticleOptions getParticle() {
        return particleEffect;
    }

    public Vec3 getSpread() {
        return spread;
    }

    public int getFrequency() {
        return frequency;
    }

    public Vec3 getOffset() {
        return offset;
    }

    public double getOffsetX() {
        return getOffset().x();
    }

    public double getOffsetY() {
        return getOffset().y();
    }

    public double getOffsetZ() {
        return getOffset().z();
    }

    public int getCount() {
        return count;
    }

    public float getSpeed() {
        return speed;
    }

    public boolean shouldForce() {
        return force;
    }

    public boolean isVisibleInFirstPerson() {
        return visibleInFirstPerson;
    }

    public boolean isVisibleWhileInvisible() {
        return visibleWhileInvisible;
    }

}
