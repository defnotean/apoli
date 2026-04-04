package io.github.apace100.apoli.action.type.entity;

import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.EntityAction;
import io.github.apace100.apoli.action.context.EntityActionContext;
import io.github.apace100.apoli.action.type.EntityActionType;
import io.github.apace100.apoli.action.type.EntityActionTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.util.MiscUtil;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ExplosiveProjectileEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 *  TODO: Add a {@code bientity_action} field -eggohito
 */
public class FireProjectileEntityActionType extends EntityActionType {

    public static final TypedDataObjectFactory<FireProjectileEntityActionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("entity_type", SerializableDataTypes.ENTITY_TYPE)
            .add("projectile_action", EntityAction.DATA_TYPE.optional(), Optional.empty())
            .add("tag", SerializableDataTypes.NBT_COMPOUND, new CompoundTag())
            .add("divergence", SerializableDataTypes.FLOAT, 1.0F)
            .add("speed", SerializableDataTypes.FLOAT, 1.5F)
            .add("count", SerializableDataTypes.INT, 1),
        data -> new FireProjectileEntityActionType(
            data.get("entity_type"),
            data.get("projectile_action"),
            data.get("tag"),
            data.get("divergence"),
            data.get("speed"),
            data.get("count")
        ),
        (actionType, serializableData) -> serializableData.instance()
            .set("entity_type", actionType.entityType)
            .set("projectile_action", actionType.projectileAction)
            .set("tag", actionType.tag)
            .set("divergence", actionType.divergence)
            .set("speed", actionType.speed)
            .set("count", actionType.count)
    );

    private final EntityType<?> entityType;
    private final Optional<EntityAction> projectileAction;

    private final CompoundTag tag;

    private final float divergence;
    private final float speed;

    private final int count;

    public FireProjectileEntityActionType(EntityType<?> entityType, Optional<EntityAction> projectileAction, CompoundTag tag, float divergence, float speed, int count) {
        this.entityType = entityType;
        this.projectileAction = projectileAction;
        this.tag = tag;
        this.divergence = divergence;
        this.speed = speed;
        this.count = count;
    }

    @Override
    public void accept(EntityActionContext context) {

        Entity entity = context.entity();

        if (!(entity.level() instanceof ServerLevel serverWorld)) {
            return;
        }

        Random random = serverWorld.getRandom();

        Vec3 velocity = entity.getDeltaMovement();
        Vec3 verticalOffset = entity.position().add(0, entity.getEyeHeight(entity.getPose()), 0);

        float pitch = entity.getXRot();
        float yaw = entity.getYRot();

        for (int i = 0; i < count; i++) {

            Entity entityToSpawn = MiscUtil
                .getEntityWithPassengers(serverWorld, entityType, tag, verticalOffset, yaw, pitch)
                .orElse(null);

            if (entityToSpawn == null) {
                return;
            }

            if (entityToSpawn instanceof Projectile projectileToSpawn) {

                if (projectileToSpawn instanceof ExplosiveProjectileEntity explosiveProjectileToSpawn) {
                    explosiveProjectileToSpawn.accelerationPower = speed;
                }

                projectileToSpawn.setOwner(entity);
                projectileToSpawn.setDeltaMovement(entity, pitch, yaw, 0F, speed, divergence);

            }

            else {

                float j = 0.017453292F;
                double k = 0.007499999832361937D;

                float l = -Mth.sin(yaw * j) * Mth.cos(pitch * j);
                float m = -Mth.sin(pitch * j);
                float n =  Mth.cos(yaw * j) * Mth.cos(pitch * j);

                Vec3 velocityToApply = new Vec3(l, m, n)
                    .normalize()
                    .add(random.nextGaussian() * k * divergence, random.nextGaussian() * k * divergence, random.nextGaussian() * k * divergence)
                    .multiply(speed);

                entityToSpawn.setDeltaMovement(velocityToApply);
                entityToSpawn.push(velocity.x, entity.onGround() ? 0.0D : velocity.y, velocity.z);

            }

            if (!tag.isEmpty()) {

                CompoundTag mergedNbt = entityToSpawn.save(new CompoundTag());
                mergedNbt.copyFrom(tag);

                entityToSpawn.load(mergedNbt);

            }

            serverWorld.spawnNewEntityAndPassengers(entityToSpawn);
            projectileAction.ifPresent(action -> action.execute(entityToSpawn));

        }

    }

    @Override
    public @NotNull ActionConfiguration<?> getConfig() {
        return EntityActionTypes.FIRE_PROJECTILE;
    }

}
