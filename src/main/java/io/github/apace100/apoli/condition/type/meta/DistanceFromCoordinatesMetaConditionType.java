package io.github.apace100.apoli.condition.type.meta;

import com.mojang.datafixers.util.Either;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.condition.Condition;
import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.context.BlockConditionContext;
import io.github.apace100.apoli.condition.context.EntityConditionContext;
import io.github.apace100.apoli.condition.type.ConditionType;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.util.Comparison;
import io.github.apace100.apoli.util.Shape;
import io.github.apace100.apoli.util.context.ConditionContext;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

/**
 *  @author Alluysl
 *  @author (refactored by) eggohito
 */
public interface DistanceFromCoordinatesMetaConditionType {

    Reference reference();
    Shape shape();

    Optional<Integer> roundToDigit();
    Vec3 offset();

    Optional<Boolean> resultOnWrongDimension();
    boolean checkModifiedSpawn();

    Comparison comparison();
    double compareTo();

    boolean scaleReferenceToDimension();
    boolean scaleDistanceToDimension();

    boolean ignoreX();
    boolean ignoreY();
    boolean ignoreZ();

    default boolean testCondition(Either<BlockConditionContext, EntityConditionContext> context) {

        Level world = context.map(BlockConditionContext::world, EntityConditionContext::world);
        BlockPos pos = context.map(BlockConditionContext::pos, EntityConditionContext::blockPos);

        double coordinateScale = world.dimensionType().coordinateScale();

        double x = 0;
        double y = 0;
        double z = 0;

        switch (reference()) {

            case PLAYER_SPAWN -> {

                //  Requires an entity context -- if there's no entity or no server player, fall back to world spawn
                Entity entity = context
                    .right()
                    .map(EntityConditionContext::entity)
                    .orElse(null);

                if (entity instanceof ServerPlayer serverPlayer) {
                    //  Use the player's set respawn position (i.e., their bed or anchor location)
                    //  If none is set, fall through to world spawn behaviour
                    ServerPlayer.RespawnConfig respawnConfig = serverPlayer.getRespawnConfig();
                    if (respawnConfig != null) {
                        BlockPos spawnPos = respawnConfig.respawnData().pos();
                        //  Check for wrong-dimension guard
                        var respawnDimension = respawnConfig.respawnData().dimension();
                        if (resultOnWrongDimension().isPresent() && !world.dimension().equals(respawnDimension)) {
                            return resultOnWrongDimension().get();
                        }
                        x = spawnPos.getX();
                        y = spawnPos.getY();
                        z = spawnPos.getZ();
                        break;
                    }
                }

                //  No player-specific spawn set -- fall back to world spawn
                BlockPos worldSpawn = world.getLevelData().getRespawnData().pos();
                x = worldSpawn.getX();
                y = worldSpawn.getY();
                z = worldSpawn.getZ();

            }

            case PLAYER_NATURAL_SPAWN -> {

                Entity entity = context
                    .right()
                    .map(EntityConditionContext::entity)
                    .orElse(null);

                if (entity instanceof ServerPlayer serverPlayer) {
                    //  Use the "natural" (unforced / no anchor) spawn position -- i.e., bed spawn only
                    //  A forced respawn (anchor or /spawnpoint) is excluded if checkModifiedSpawn() is true
                    ServerPlayer.RespawnConfig respawnConfig = serverPlayer.getRespawnConfig();

                    if (respawnConfig != null && (!checkModifiedSpawn() || !respawnConfig.forced())) {
                        BlockPos spawnPos = respawnConfig.respawnData().pos();
                        var respawnDimension = respawnConfig.respawnData().dimension();
                        if (resultOnWrongDimension().isPresent() && !world.dimension().equals(respawnDimension)) {
                            return resultOnWrongDimension().get();
                        }
                        x = spawnPos.getX();
                        y = spawnPos.getY();
                        z = spawnPos.getZ();
                        break;
                    }
                }

                //  No natural spawn set -- fall back to world spawn
                BlockPos worldSpawn = world.getLevelData().getRespawnData().pos();
                x = worldSpawn.getX();
                y = worldSpawn.getY();
                z = worldSpawn.getZ();

            }

            case WORLD_SPAWN -> {

                //  Dimension guard: result_on_wrong_dimension only applies in the overworld context
                if (resultOnWrongDimension().isPresent() && !world.dimension().equals(Level.OVERWORLD)) {
                    return resultOnWrongDimension().get();
                }

                BlockPos spawnPos = world.getLevelData().getRespawnData().pos();
                x = spawnPos.getX();
                y = spawnPos.getY();
                z = spawnPos.getZ();

            }

            case WORLD_ORIGIN -> {
                //  The origin of a world is always (0, 0, 0); nothing to set
            }

        }

        x += offset().x();
        y += offset().y();
        z += offset().z();

        if (scaleReferenceToDimension() && (x != 0 || z != 0)) {
            x /= coordinateScale;
            z /= coordinateScale;
        }

        double xDistance = ignoreX() ? 0 : Math.abs(pos.getX() - x);
        double yDistance = ignoreY() ? 0 : Math.abs(pos.getY() - y);
        double zDistance = ignoreZ() ? 0 : Math.abs(pos.getZ() - z);

        if (scaleDistanceToDimension()) {
            xDistance *= coordinateScale;
            zDistance *= coordinateScale;
        }

        double distance = shape().getDistance(xDistance, yDistance, zDistance);
        double scaledDistance = roundToDigit()
            .map(scale -> new BigDecimal(distance).setScale(scale, RoundingMode.HALF_UP).doubleValue())
            .orElse(distance);

        return comparison().compare(scaledDistance, compareTo());

    }

    static <T extends ConditionContext, C extends Condition<T, CT>, CT extends ConditionType<T, C>, M extends ConditionType<T, C> & DistanceFromCoordinatesMetaConditionType> ConditionConfiguration<M> createConfiguration(Constructor<M> constructor) {
        return ConditionConfiguration.of(
            Apoli.identifier("distance_from_coordinates"),
            new SerializableData()
                .add("reference", SerializableDataType.enumValue(Reference.class), Reference.WORLD_ORIGIN)
                .add("shape", SerializableDataType.enumValue(Shape.class), Shape.CUBE)
                .add("round_to_digit", SerializableDataTypes.INT.optional(), Optional.empty())
                .add("offset", SerializableDataTypes.VECTOR, Vec3.ZERO)
                .add("comparison", ApoliDataTypes.COMPARISON)
                .add("compare_to", SerializableDataTypes.DOUBLE)
                .add("result_on_wrong_dimension", SerializableDataTypes.BOOLEAN.optional(), Optional.empty())
                .add("check_modified_spawn", SerializableDataTypes.BOOLEAN, true)
                .add("scale_reference_to_dimension", SerializableDataTypes.BOOLEAN, true)
                .add("scale_distance_to_dimension", SerializableDataTypes.BOOLEAN, false)
                .add("ignore_x", SerializableDataTypes.BOOLEAN, false)
                .add("ignore_y", SerializableDataTypes.BOOLEAN, false)
                .add("ignore_z", SerializableDataTypes.BOOLEAN, false),
            data -> constructor.create(
                data.get("reference"),
                data.get("shape"),
                data.get("round_to_digit"),
                data.get("offset"),
                data.get("comparison"),
                data.get("compare_to"),
                data.get("result_on_wrong_dimension"),
                data.get("check_modified_spawn"),
                data.get("scale_reference_to_dimension"),
                data.get("scale_distance_to_dimension"),
                data.get("ignore_x"),
                data.get("ignore_y"),
                data.get("ignore_z")
            ),
            (m, serializableData) -> serializableData.instance()
                .set("reference", m.reference())
                .set("shape", m.shape())
                .set("round_to_digit", m.roundToDigit())
                .set("offset", m.offset())
                .set("comparison", m.comparison())
                .set("compare_to", m.compareTo())
                .set("result_on_wrong_dimension", m.resultOnWrongDimension())
                .set("check_modified_spawn", m.checkModifiedSpawn())
                .set("scale_reference_to_dimension", m.scaleReferenceToDimension())
                .set("scale_distance_to_dimension", m.scaleDistanceToDimension())
                .set("ignore_x", m.ignoreX())
                .set("ignore_y", m.ignoreY())
                .set("ignore_z", m.ignoreZ())
        );
    }

    interface Constructor<M extends ConditionType<?, ?> & DistanceFromCoordinatesMetaConditionType> {
        M create(Reference reference, Shape shape, Optional<Integer> roundToDigit, Vec3 offset,
                 Comparison comparison, double compareTo,
                 Optional<Boolean> resultOnWrongDimension, boolean checkModifiedSpawn,
                 boolean scaleReferenceToDimension, boolean scaleDistanceToDimension,
                 boolean ignoreX, boolean ignoreY, boolean ignoreZ);
    }

    enum Reference {
        PLAYER_SPAWN,
        PLAYER_NATURAL_SPAWN,
        WORLD_SPAWN,
        WORLD_ORIGIN
    }

}
