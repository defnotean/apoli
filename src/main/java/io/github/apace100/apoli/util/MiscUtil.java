package io.github.apace100.apoli.util;

import com.mojang.serialization.DataResult;
import io.github.apace100.apoli.condition.Condition;
import io.github.apace100.apoli.condition.context.BlockConditionContext;
import io.github.apace100.apoli.condition.type.ConditionType;
import io.github.apace100.apoli.condition.type.meta.MultiMetaConditionType;
import io.github.apace100.apoli.util.context.ConditionContext;
import io.github.apace100.calio.data.SerializableData;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.world.level.block.BlockRenderType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnReason;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.inventory.SlotRange;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Holder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionBehavior;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public final class MiscUtil {

    public static void createExplosion(Level world, Vec3 pos, float power, boolean createFire, Explosion.BlockInteraction destructionType, ExplosionBehavior behavior) {
        createExplosion(world, null, pos, power, createFire, destructionType, behavior);
    }

    public static void createExplosion(Level world, Entity entity, Vec3 pos, float power, boolean createFire, Explosion.BlockInteraction destructionType, ExplosionBehavior behavior) {
        createExplosion(world, entity, null, pos.getX(), pos.getY(), pos.getZ(), power, createFire, destructionType, behavior);
    }

    public static void createExplosion(Level world, @Nullable Entity entity, @Nullable DamageSource damageSource, double x, double y, double z, float power, boolean createFire, Explosion.BlockInteraction destructionType, ExplosionBehavior behavior) {

        Explosion explosion = new Explosion(world, entity, damageSource, behavior, x, y, z, power, createFire, destructionType, ParticleTypes.EXPLOSION, ParticleTypes.EXPLOSION_EMITTER, SoundEvents.ENTITY_GENERIC_EXPLODE);

        explosion.collectBlocksAndDamageEntities();
        explosion.affectWorld(world.isClientSide);

        //  Sync the explosion effect to the client if the explosion is created on the server
        if (!(world instanceof ServerLevel serverWorld)) {
            return;
        }

        if (!explosion.shouldDestroy()) {
            explosion.clearAffectedBlocks();
        }

        for (ServerPlayer serverPlayerEntity : serverWorld.players()) {
            if (serverPlayerEntity.distanceToSqr(x, y, z) < 4096.0) {
                serverPlayerEntity.connection.send(new ClientboundExplodePacket(x, y, z, power, explosion.getAffectedBlocks(), explosion.getAffectedPlayers().get(serverPlayerEntity), explosion.getBlockInteraction(), explosion.getParticle(), explosion.getEmitterParticle(), explosion.getSoundEvent()));
            }
        }

    }

    @Nullable
    public static ExplosionBehavior getExplosionBehavior(Level world, float indestructibleResistance, @Nullable Predicate<BlockInWorld> indestructibleCondition) {
        return indestructibleCondition == null ? null : new ExplosionBehavior() {

            @Override
            public Optional<Float> getBlastResistance(Explosion explosion, BlockGetter blockView, BlockPos pos, BlockState blockState, FluidState fluidState) {

                BlockInWorld cachedBlockPosition = new BlockInWorld(world, pos, true);

                Optional<Float> defaultValue = super.getBlastResistance(explosion, world, pos, blockState, fluidState);
                Optional<Float> newValue = indestructibleCondition.test(cachedBlockPosition) ? Optional.of(indestructibleResistance) : Optional.empty();

                return defaultValue.isPresent() ? (newValue.isPresent() ? (defaultValue.get() > newValue.get() ? (defaultValue) : newValue) : defaultValue) : defaultValue;

            }

            @Override
            public boolean canDestroyBlock(Explosion explosion, BlockGetter blockView, BlockPos pos, BlockState state, float power) {
                return !indestructibleCondition.test(new BlockInWorld(world, pos, true));
            }

        };
    }

    @Nullable
    public static ExplosionBehavior createExplosionBehavior(@Nullable Predicate<BlockConditionContext> indestructibleCondition, float resistance) {
        return indestructibleCondition == null ? null : new ExplosionBehavior() {

            @Override
            public Optional<Float> getBlastResistance(Explosion explosion, BlockGetter world, BlockPos pos, BlockState blockState, FluidState fluidState) {

                Optional<Float> defaultValue = super.getBlastResistance(explosion, world, pos, blockState, fluidState);
                Optional<Float> newValue = indestructibleCondition.test(new BlockConditionContext((Level) world, pos))
                    ? Optional.of(resistance)
                    : Optional.empty();

                return defaultValue
                    .flatMap(defVal -> newValue
                        .map(newVal -> defVal > newVal ? defVal : newVal));

            }

            @Override
            public boolean canDestroyBlock(Explosion explosion, BlockGetter world, BlockPos pos, BlockState state, float power) {
                return !indestructibleCondition.test(new BlockConditionContext((Level) world, pos));
            }

        };
    }

    public static Optional<Entity> getEntityWithPassengers(Level world, EntityType<?> entityType, @Nullable CompoundTag entityNbt, Vec3 pos, float yaw, float pitch) {
        return getEntityWithPassengers(world, entityType, entityNbt, pos, Optional.of(yaw), Optional.of(pitch));
    }

    public static Optional<Entity> getEntityWithPassengers(Level world, EntityType<?> entityType, @Nullable CompoundTag entityNbt, Vec3 pos, Optional<Float> yaw, Optional<Float> pitch) {

        if (!(world instanceof ServerLevel serverWorld)) {
            return Optional.empty();
        }

        CompoundTag entityToSpawnNbt = new CompoundTag();
        if (entityNbt != null && !entityNbt.isEmpty()) {
            entityToSpawnNbt.copyFrom(entityNbt);
        }

        entityToSpawnNbt.putString("id", BuiltInRegistries.ENTITY_TYPE.getId(entityType).toString());
        Entity entityToSpawn = EntityType.loadEntityWithPassengers(
            entityToSpawnNbt,
            serverWorld,
            entity -> {
                entity.moveTo(pos.x, pos.y, pos.z, yaw.orElse(entity.getYRot()), pitch.orElse(entity.getXRot()));
                return entity;
            }
        );

        if (entityToSpawn == null) {
            return Optional.empty();
        }

        if ((entityNbt == null || entityNbt.isEmpty()) && entityToSpawn instanceof Mob mobToSpawn) {
            mobToSpawn.initialize(serverWorld, serverWorld.getLocalDifficulty(BlockPos.ofFloored(pos)), SpawnReason.COMMAND, null);
        }

        return Optional.of(entityToSpawn);

    }

    @Nullable
    public static Entity getEntityByUuid(@Nullable UUID uuid, @Nullable MinecraftServer server) {

        if (uuid == null || server == null) {
            return null;
        }

        Entity entity;
        for (ServerLevel serverWorld : server.getAllLevels()) {

            if ((entity = serverWorld.getEntity(uuid)) != null) {
                return entity;
            }

        }

        return null;

    }

    public static BlockState getInWallBlockState(Entity playerEntity) {
        BlockPos.Mutable mutable = new BlockPos.Mutable();

        for(int i = 0; i < 8; ++i) {
            double d = playerEntity.getX() + (double)(((float)((i >> 0) % 2) - 0.5F) * playerEntity.getWidth() * 0.8F);
            double e = playerEntity.getEyeY() + (double)(((float)((i >> 1) % 2) - 0.5F) * 0.1F);
            double f = playerEntity.getZ() + (double)(((float)((i >> 2) % 2) - 0.5F) * playerEntity.getWidth() * 0.8F);
            mutable.set(d, e, f);
            BlockState blockState = playerEntity.level().getBlockState(mutable);
            if (blockState.getRenderType() != BlockRenderType.INVISIBLE && blockState.shouldBlockVision(playerEntity.level(), mutable)) {
                return blockState;
            }
        }

        return null;
    }

    public static <T> Predicate<T> combineOr(Predicate<T> a, Predicate<T> b) {
        if(a == null) {
            return b;
        }
        if(b == null) {
            return a;
        }
        return a.or(b);
    }

    public static <T> Predicate<T> combineAnd(Predicate<T> a, Predicate<T> b) {
        if(a == null) {
            return b;
        }
        if(b == null) {
            return a;
        }
        return a.and(b);
    }

    public static boolean allPresent(SerializableData.Instance data, String... fieldNames) {

        Set<String> fieldsToEvaluate = fieldNames.length > 0
            ? new ObjectOpenHashSet<>(fieldNames)
            : data.serializableData().getFieldNames();

        for (String field : fieldsToEvaluate) {

            if (!data.isPresent(field)) {
                return false;
            }

        }

        return true;

    }

    public static Function<SerializableData.Instance, DataResult<SerializableData.Instance>> validateAllFieldsPresent(String... fields) {
        return data -> {

            if (allPresent(data, fields)) {
                return DataResult.success(data);
            }

            else {

                StringBuilder message = new StringBuilder(fields.length > 1 ? "All of " : "The ");
                String separator = "";

                for (int i = 0; i < fields.length; i++) {

                    String field = fields[i];
                    message
                        .append(separator)
                        .append("'").append(field).append("'");

                    separator = i == fields.length - 2
                        ? ", and "
                        : ", ";

                }

                message
                    .append(" field").append(fields.length > 1 ? "s" : "")
                    .append(" must be defined!");

                return DataResult.error(message::toString);

            }

        };
    }

    public static boolean anyPresent(SerializableData.Instance data, String... fieldNames) {

        Set<String> fieldsToEvaluate = fieldNames.length > 0
            ? new ObjectOpenHashSet<>(fieldNames)
            : data.serializableData().getFieldNames();

        for (String field : fieldsToEvaluate) {

            if (data.isPresent(field)) {
                return true;
            }

        }

        return false;

    }

    public static Function<SerializableData.Instance, DataResult<SerializableData.Instance>> validateAnyFieldsPresent(String... fields) {
        return data -> {

            if (anyPresent(data, fields)) {
                return DataResult.success(data);
            }

            else {

                StringBuilder message = new StringBuilder(fields.length > 1 ? "Any of the " : "The ");
                String separator = "";

                for (int i = 0; i < fields.length; i++) {

                    String field = fields[i];
                    message
                        .append(separator)
                        .append("'").append(field).append("'");

                    separator = i == fields.length - 2
                        ? ", and "
                        : ", ";

                }

                message
                    .append(" field").append(fields.length > 1 ? "s" : "")
                    .append(" must be defined!");

                return DataResult.error(message::toString);

            }

        };
    }

    public static OptionalInt getSpaceInInventory(Player player, ItemStack stack) {
        return getSpaceInInventory(player.getInventory(), stack);
    }

    public static OptionalInt getSpaceInInventory(Inventory playerInventory, ItemStack stack) {

        int slot = playerInventory.getOccupiedSlotWithRoomForStack(stack);
        if (slot == -1) {
            slot = playerInventory.getEmptySlot();
        }

        return slot == -1
            ? OptionalInt.empty()
            : OptionalInt.of(slot);

    }

    public static boolean hasSpaceInInventory(Player player, ItemStack stack) {
        return getSpaceInInventory(player, stack).isPresent();
    }

    public static boolean hasSpaceInInventory(Inventory playerInventory, ItemStack stack) {
        return getSpaceInInventory(playerInventory, stack).isPresent();
    }

    public static <E, C extends Collection<E>> BinaryOperator<C> mergeCollections() {
        return (coll1, coll2) -> {
            coll1.addAll(coll2);
            return coll1;
        };
    }

    @Nullable
    public static <T> List<T> singletonListOrNull(@Nullable T value) {
        return mapOr(value, List::of, () -> null);
    }

    public static <T> List<T> singletonListOrEmpty(@Nullable T value) {
        return mapOr(value, List::of, List::of);
    }

    public static <T, U> U mapOr(@Nullable T value, Function<T, U> mapper, Supplier<U> defaultValue) {
        return Optional.ofNullable(value)
            .map(mapper)
            .orElseGet(defaultValue);
    }

    public static IntSet toSlotIdSet(Collection<SlotRange> slotRanges) {

        IntSet slotIdSet = new IntOpenHashSet();
        for (SlotRange slotRange : slotRanges) {
            slotIdSet.addAll(slotRange.getSlotIds());
        }

        return slotIdSet;

    }

    public static Vec3 getPoseDependentEyePos(Entity entity) {
        return new Vec3(entity.getX(), entity.getY() + entity.getEyeHeight(entity.getPose()), entity.getZ());
    }

    public static double getAttributeValueOrElse(Entity entity, Holder<Attribute> attribute, double defaultValue) {

        if (entity instanceof LivingEntity livingEntity && livingEntity.getAttributes().hasAttribute(attribute)) {
            return livingEntity.getAttributeValue(attribute);
        }

        else {
            return defaultValue;
        }

    }

    public static <CX extends ConditionContext, CC extends Condition<CX, CT>, CT extends ConditionType<CX, CC>> DataResult<CT> validateConditionType(CT conditionType, Function<CT, DataResult<CT>> validator) {

        if (conditionType instanceof MultiMetaConditionType<?, ?> multi) {

            for (var innerCondition : multi.conditions()) {

                CT innerConditionType = (CT) innerCondition.getType();
                DataResult<CT> result = validateConditionType(innerConditionType, validator);

                if (result.isError()) {
                    return result;
                }

            }

            return DataResult.success(conditionType);

        }

        else {
            return validator.apply(conditionType);
        }

    }

}
