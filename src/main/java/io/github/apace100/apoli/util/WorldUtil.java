package io.github.apace100.apoli.util;

import io.github.apace100.apoli.access.BlockCollisionsAccess;
import net.minecraft.world.entity.Entity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.BlockCollisions;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class WorldUtil {

    public static Iterable<VoxelShape> getOriginalBlockCollisions(Level world, @Nullable Entity entity, AABB box) {

        BlockCollisions<VoxelShape> spliterator = new BlockCollisions<>(world, entity, box, false, (pos, voxelShape) -> voxelShape);
        ((BlockCollisionsAccess) spliterator).apoli$setGetOriginalShapes(true);

        return () -> spliterator;

    }

    public static boolean inSnow(Level world, BlockPos... blockPositions) {
        return Arrays.stream(blockPositions)
            .anyMatch(blockPos -> {
                Biome biome = world.getBiome(blockPos).value();
                return biome.getPrecipitation(blockPos) == Biome.Precipitation.SNOW
                    && isRainingAndExposed(world, blockPos);
            });
    }

    public static boolean inThunderstorm(Level world, BlockPos... blockPositions) {
        return Arrays.stream(blockPositions)
            .anyMatch(blockPos -> world.isThundering() && isRainingAndExposed(world, blockPos));
    }

    private static boolean isRainingAndExposed(Level world, BlockPos blockPos) {
        return world.isRaining()
            && world.canSeeSky(blockPos)
            && world.getHeightmapPos(Heightmap.Type.MOTION_BLOCKING, blockPos).y() < blockPos.y();
    }

}
