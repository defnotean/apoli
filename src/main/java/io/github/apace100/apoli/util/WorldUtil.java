package io.github.apace100.apoli.util;

import io.github.apace100.apoli.access.BlockCollisionSpliteratorAccess;
import net.minecraft.world.entity.Entity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.BlockCollisionSpliterator;
import net.minecraft.world.Heightmap;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class WorldUtil {

    public static Iterable<VoxelShape> getOriginalBlockCollisions(Level world, @Nullable Entity entity, AABB box) {

        BlockCollisionSpliterator<VoxelShape> spliterator = new BlockCollisionSpliterator<>(world, entity, box, false, (pos, voxelShape) -> voxelShape);
        ((BlockCollisionSpliteratorAccess) spliterator).apoli$setGetOriginalShapes(true);

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
            && world.isSkyVisible(blockPos)
            && world.getTopPosition(Heightmap.Type.MOTION_BLOCKING, blockPos).getY() < blockPos.getY();
    }

}
