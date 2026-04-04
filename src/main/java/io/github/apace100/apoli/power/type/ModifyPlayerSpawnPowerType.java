package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.power.PowerConfiguration;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.world.entity.Dismounting;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.tags.TagKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import com.mojang.datafixers.util.Pair;
import net.minecraft.util.Unit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.ChunkPos;
import net.minecraft.core.ChunkSectionPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.apache.commons.lang3.function.TriFunction;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.core.registries.Registries;

public class ModifyPlayerSpawnPowerType extends PowerType implements Prioritized<ModifyPlayerSpawnPowerType> {

    public static final TypedDataObjectFactory<ModifyPlayerSpawnPowerType> DATA_FACTORY = createConditionedDataFactory(
        new SerializableData()
            .add("dimension", SerializableDataTypes.DIMENSION)
            .add("structure", SerializableDataType.registryKey(Registries.STRUCTURE).optional(), Optional.empty())
            .add("structure_tag", SerializableDataType.tagKey(Registries.STRUCTURE).optional(), Optional.empty())
            .add("biome", SerializableDataType.registryKey(Registries.BIOME).optional(), Optional.empty())
            .add("biome_tag", SerializableDataType.tagKey(Registries.BIOME).optional(), Optional.empty())
            .add("spawn_strategy", SerializableDataType.enumValue(SpawnStrategy.class), SpawnStrategy.DEFAULT)
            .add("respawn_sound", SerializableDataTypes.SOUND_EVENT.optional(), Optional.empty())
            .add("dimension_distance_multiplier", SerializableDataTypes.NON_NEGATIVE_FLOAT, 1.0F)
            .add("priority", SerializableDataTypes.INT, 0),
        (data, condition) -> new ModifyPlayerSpawnPowerType(
            data.get("dimension"),
            data.get("structure"),
            data.get("structure_tag"),
            data.get("biome"),
            data.get("biome_tag"),
            data.get("spawn_strategy"),
            data.get("respawn_sound"),
            data.get("dimension_distance_multiplier"),
            data.get("priority"),
            condition
        ),
        (powerType, serializableData) -> serializableData.instance()
            .set("dimension", powerType.dimensionKey)
            .set("structure", powerType.structureKey)
            .set("structure_tag", powerType.structureTag)
            .set("biome", powerType.biomeKey)
            .set("biome_tag", powerType.biomeTag)
            .set("spawn_strategy", powerType.spawnStrategy)
            .set("respawn_sound", powerType.respawnSound)
            .set("dimension_distance_multiplier", powerType.dimensionDistanceMultiplier)
            .set("priority", powerType.getPriority())
    );

    private final ResourceKey<Level> dimensionKey;

    private final Optional<ResourceKey<Structure>> structureKey;
    private final Optional<TagKey<Structure>> structureTag;

    private final Optional<ResourceKey<Biome>> biomeKey;
    private final Optional<TagKey<Biome>> biomeTag;

    private final SpawnStrategy spawnStrategy;
    private final Optional<SoundEvent> respawnSound;

    private final float dimensionDistanceMultiplier;
    private final int priority;

    public ModifyPlayerSpawnPowerType(ResourceKey<Level> dimensionKey, Optional<ResourceKey<Structure>> structureKey, Optional<TagKey<Structure>> structureTag, Optional<ResourceKey<Biome>> biomeKey, Optional<TagKey<Biome>> biomeTag, SpawnStrategy spawnStrategy, Optional<SoundEvent> respawnSound, float dimensionDistanceMultiplier, int priority, Optional<EntityCondition> condition) {
        super(condition);
        this.dimensionKey = dimensionKey;
        this.structureKey = structureKey;
        this.structureTag = structureTag;
        this.biomeKey = biomeKey;
        this.biomeTag = biomeTag;
        this.spawnStrategy = spawnStrategy;
        this.respawnSound = respawnSound;
        this.dimensionDistanceMultiplier = dimensionDistanceMultiplier;
        this.priority = priority;
    }

    @Override
    public @NotNull PowerConfiguration<?> getConfig() {
        return PowerTypes.MODIFY_PLAYER_SPAWN;
    }

    @Override
    public void onRespawn() {
        LivingEntity entity = getHolder();
		respawnSound.ifPresent(soundEvent -> entity.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(), soundEvent, entity.getSoundSource(), 1.0F, 1.0F));
    }

    @Override
    public void onLost() {

        if (!(getHolder() instanceof ServerPlayer serverPlayer)) {
            return;
        }

        if (!serverPlayer.isDisconnected() && serverPlayer.getRespawnPosition() != null && !serverPlayer.isRespawnForced()) {
            serverPlayer.setRespawnPosition(Level.OVERWORLD, null, 0F, false, false);
        }

    }

    @Override
    public int getPriority() {
        return priority;
    }

    public ResourceKey<Level> getDimensionKey() {
        return dimensionKey;
    }

    public void teleportToModifiedSpawn() {

        if (!(getHolder() instanceof ServerPlayer serverPlayer)) {
            return;
        }

        Pair<ServerLevel, BlockPos> spawnPoint = this.getSpawn().orElse(null);
        if (spawnPoint == null) {
            return;
        }

        ServerLevel spawnPointDimension = spawnPoint.getLeft();
        BlockPos spawnPointPosition = spawnPoint.getRight();

        float pitch = serverPlayer.getXRot();
        float yaw = serverPlayer.getYRot();

        Vec3 placement = Dismounting.findRespawnPos(serverPlayer.getType(), spawnPointDimension, spawnPointPosition, true);
        if (placement == null) {
            Apoli.LOGGER.warn("Power \"{}\" could not find a suitable spawn point for player {}! Teleporting to the found location directly...", this.getPower().getId(), serverPlayer.getName().getString());
            serverPlayer.teleport(spawnPointDimension, spawnPointPosition.getX(), spawnPointPosition.getY(), spawnPointPosition.getZ(), pitch, yaw);
        }

        else {
            serverPlayer.teleport(spawnPointDimension, placement.getX(), placement.getY(), placement.getZ(), pitch, yaw);
        }

    }

    public Optional<Pair<ServerLevel, BlockPos>> getSpawn() {

        if (!(getHolder() instanceof ServerPlayer serverPlayer)) {
            return Optional.empty();
        }

        MinecraftServer server = serverPlayer.server;
        ServerLevel targetDimension = server.getLevel(dimensionKey);

        if (targetDimension == null) {
            return Optional.empty();
        }

        int center = targetDimension.getLogicalHeight() / 2;
        int range = 64;

        AtomicReference<Vec3> newSpawnPointVec = new AtomicReference<>();
        BlockPos dimensionSpawnPos = serverPlayer.serverLevel().getSharedSpawnPos();

        BlockPos.Mutable newSpawnPointPos = new BlockPos.Mutable();
        BlockPos.Mutable mutableDimensionSpawnPos = spawnStrategy.apply(dimensionSpawnPos, center, dimensionDistanceMultiplier).mutable();

        this.getBiomePos(targetDimension, mutableDimensionSpawnPos).ifPresent(mutableDimensionSpawnPos::set);
        this.getSpawnPos(targetDimension, mutableDimensionSpawnPos, range).ifPresent(newSpawnPointVec::set);

        if (newSpawnPointVec.get() == null) {
            return Optional.empty();
        }

        Vec3 msp = newSpawnPointVec.get();
        newSpawnPointPos.set(msp.x, msp.y, msp.z);

        targetDimension.getChunkSource().addTicket(TicketType.START, new ChunkPos(newSpawnPointPos), 11, Unit.INSTANCE);
        return Optional.of(new Pair<>(targetDimension, newSpawnPointPos));

    }

    private Optional<BlockPos> getBiomePos(ServerLevel targetDimension, BlockPos originPos) {

        if (biomeKey.isEmpty() && biomeTag.isEmpty()) {
            return Optional.empty();
        }

        int radius = Apoli.config.modifyPlayerSpawnPower.radius;
        int horizontalBlockCheckInterval = Apoli.config.modifyPlayerSpawnPower.horizontalBlockCheckInterval;
        int verticalBlockCheckInterval = Apoli.config.modifyPlayerSpawnPower.verticalBlockCheckInterval;

        if (radius < 0) {
            radius = 6400;
        }

        if (horizontalBlockCheckInterval <= 0) {
            horizontalBlockCheckInterval = 64;
        }

        if (verticalBlockCheckInterval <= 0) {
            verticalBlockCheckInterval = 64;
        }

        var targetBiomePos = targetDimension.locateBiome(
            biome -> biomeKey.map(biome::matchesKey).orElse(false) || biomeTag.map(biome::isIn).orElse(false),
            originPos,
            radius,
            horizontalBlockCheckInterval,
            verticalBlockCheckInterval
        );

        if (targetBiomePos != null) {
            return Optional.of(targetBiomePos.getFirst());
        }

        else {

            LivingEntity holder = getHolder();
            StringBuilder name = new StringBuilder();

            biomeKey
                .map(ResourceKey::location)
                .ifPresent(id -> name.append("biome \"").append(id).append("\""));
            biomeTag
                .map(TagKey::location)
                .ifPresent(id -> name.append(!name.isEmpty() ? " or " : "").append("any biomes from tag \"").append(id).append("\""));

            Apoli.LOGGER.warn("Power \"{}\" could not set player {}'s spawn point at {} as none can be found nearby in dimension \"{}\".", this.getPower().getId(), holder.getName().getString(), name, dimensionKey.location());
            holder.sendSystemMessage(Component.literal("Power \"%s\" couldn't set spawn point at %s as none can be found nearby in dimension \"%s\"!".formatted(this.getPower().getId(), name, dimensionKey.location())).withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY));

            return Optional.empty();

        }

    }

    private Optional<Pair<BlockPos, Structure>> getStructurePos(ServerLevel dimension) {

        if (structureKey.isEmpty() && structureTag.isEmpty()) {
            return Optional.empty();
        }

        Registry<Structure> structureRegistry = dimension.registryAccess().get(Registries.STRUCTURE);
        List<Holder<Structure>> structureEntries = new ArrayList<>();

        structureKey
            .map(structureRegistry::entryOf)
            .ifPresent(structureEntries::add);

        structureTag
            .flatMap(structureRegistry::getEntryList)
            .map(registryEntries -> (HolderSet.ListBacked<Structure>) registryEntries)
            .map(HolderSet.ListBacked::getEntries)
            .ifPresent(structureEntries::addAll);

        BlockPos center = new BlockPos(0, 70, 0);
        int radius = Apoli.config.modifyPlayerSpawnPower.radius;

        if (radius < 0) {
            radius = 6400;
        }

        var result = Optional.ofNullable(dimension.getChunkSource().getChunkGenerator().locateStructure(dimension, HolderSet.of(structureEntries), center, radius, false))
            .map(pair -> pair.mapSecond(Holder::value))
            .map(pair -> new Pair<>(pair.getFirst(), pair.getSecond()));

        if (result.isEmpty()) {

            LivingEntity holder = getHolder();
            StringBuilder name = new StringBuilder();

            structureKey
                .map(ResourceKey::location)
                .ifPresent(id -> name.append("structure \"").append(id).append("\""));
            structureTag
                .map(TagKey::location)
                .ifPresent(id -> name.append(!name.isEmpty() ? " or " : "").append("any structures from tag \"").append(id).append("\""));

            Apoli.LOGGER.warn("Power \"{}\" could not set player {}'s spawn point at {} as none can be found nearby in dimension \"{}\".", this.getPower().getId(), holder.getName().getString(), name, dimensionKey.location());
            holder.sendSystemMessage(Component.literal("Power \"%s\" couldn't set spawn point at %s as none can be found nearby in dimension \"%s\"!".formatted(this.getPower().getId(), name, dimensionKey.location())).withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY));

            return Optional.empty();

        }

        return result;

    }

    private Optional<Vec3> getSpawnPos(ServerLevel targetDimension, BlockPos originPos, int range) {

        if (structureKey.isEmpty() && structureTag.isEmpty()) {
            return this.getValidSpawn(targetDimension, originPos, range);
        }

        Optional<Pair<BlockPos, Structure>> targetStructure = getStructurePos(targetDimension);
        if (targetStructure.isEmpty()) {
            return Optional.empty();
        }

        BlockPos structurePos = targetStructure.get().getLeft();
        Structure structure = targetStructure.get().getRight();

        ChunkPos chunkPos = new ChunkPos(structurePos.getX() >> 4, structurePos.getZ() >> 4);
        ChunkSectionPos chunkSectionPos = ChunkSectionPos.from(chunkPos, 0);

        return Optional.ofNullable(targetDimension.getStructureAccessor().getStructureStart(chunkSectionPos, structure, targetDimension.getChunk(structurePos)))
            .map(structureStart -> structureStart.getBoundingBox().getCenter())
            .flatMap(pos -> this.getValidSpawn(targetDimension, pos, range));

    }

    private Optional<Vec3> getValidSpawn(ServerLevel targetDimension, BlockPos startPos, int range) {

        //  The 'direction' vector that determines the direction of the iteration
        int dx = 1;
        int dz = 0;

        //  The length of the current segment
        int segmentLength = 1;

        //  The center of the structure/dimension
        int center = startPos.getY();

        //  The valid spawn position and (mutable) starting position
        Vec3 spawnPos;
        BlockPos.Mutable mutableStartPos = startPos.mutable();

        //  The current position
        int x = startPos.getX();
        int z = startPos.getZ();

        //  Determines how much of the current segment has been passed
        int segmentPassed = 0;

        //  Vertical offsets
        int upOffset = 0;
        int downOffset = 0;

        //  The min and max Y values of the target dimension
        int maxY = targetDimension.getLogicalHeight();
        int minY = targetDimension.dimensionTypeRegistration().value().minY();

        while (upOffset < maxY || downOffset > minY) {

            for (int steps = 0; steps < range; ++steps) {

                //  Make a step by adding the 'direction' vector to the current position
                x += dx;
                z += dz;
                mutableStartPos.setX(x);
                mutableStartPos.setZ(z);

                //  Increment how much of the current segment has been passed
                ++segmentPassed;

                //  Offset the Y axis (up and down) of the current position to check for valid spawn positions
                mutableStartPos.setY(center + upOffset);
                spawnPos = Dismounting.findRespawnPos(getHolder().getType(), targetDimension, mutableStartPos, true);

                if (spawnPos != null) {
                    return Optional.of(spawnPos);
                }

                mutableStartPos.setY(center + downOffset);
                spawnPos = Dismounting.findRespawnPos(getHolder().getType(), targetDimension, mutableStartPos, true);

                if (spawnPos != null) {
                    return Optional.of(spawnPos);
                }

                //  If the current segment has not been passed, continue the loop
                if (segmentPassed != segmentLength) continue;

                //  Otherwise, reset the value of how much of the current segment has been passed
                segmentPassed = 0;

                //  'Rotate' the 'direction' vector
                int bdx = dx;
                dx = -dz;
                dz = bdx;

                //  Increment the length of the current segment if necessary
                if (dz == 0) ++segmentLength;

            }

            //  Increment/decrement the up/down offsets until it's no longer less/greater than the max/min Y
            if (upOffset < maxY) {
                upOffset++;
            }

            if (downOffset > minY) {
                downOffset--;
            }

        }

        return Optional.empty();

    }

    public enum SpawnStrategy {

        CENTER((blockPos, center, multiplier) -> new BlockPos(0, center, 0)),
        DEFAULT((blockPos, center, multiplier) -> {

            BlockPos.Mutable mut = new BlockPos.Mutable();
            multiplier = Math.max(multiplier, 1F);

            return mut.set(blockPos.getX() * multiplier, blockPos.getY(), blockPos.getZ() * multiplier);

        });

        final TriFunction<BlockPos, Integer, Float, BlockPos> strategyApplier;
        SpawnStrategy(TriFunction<BlockPos, Integer, Float, BlockPos> strategyApplier) {
            this.strategyApplier = strategyApplier;
        }

        public BlockPos apply(BlockPos blockPos, int center, float multiplier) {
            return strategyApplier.apply(blockPos, center, multiplier);
        }

    }

}
