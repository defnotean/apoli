package io.github.apace100.apoli.util;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;

import java.util.Collection;
import java.util.Set;

public enum Shape {

    CUBE, CHEBYSHEV,
    STAR, MANHATTAN,
    SPHERE, EUCLIDEAN;

    public final Collection<BlockPos> getBlockPositions(BlockPos center, int radius) {

        ObjectOpenHashSet<BlockPos> blockPositions = new ObjectOpenHashSet<>();

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {

                    if (this.getBlockDistance(x, y, z) <= radius) {
                        blockPositions.add(new BlockPos(center.offset(x, y, z)));
                    }

                }
            }
        }

        blockPositions.trim();
        return blockPositions;

    }

    public final Collection<Entity> getEntities(Level world, Vec3 center, double radius) {

        ObjectOpenHashSet<Entity> entities = new ObjectOpenHashSet<>();

        double diameter = radius * 2;
        double x, y, z;

        for (Entity entity : world.getEntities((Entity) null, AABB.ofSize(center, diameter, diameter, diameter), EntitySelector.NO_SPECTATORS)) {

            x = Math.abs(entity.getX() - center.x());
            y = Math.abs(entity.getY() - center.y());
            z = Math.abs(entity.getZ() - center.z());

            if (this.getDistance(x, y, z) <= radius + 1) {
                entities.add(entity);
            }

        }

        entities.trim();
        return entities;

    }

    private double getBlockDistance(int x, int y, int z) {
        return switch (this) {
            case CUBE, CHEBYSHEV ->
                0;
            case STAR, MANHATTAN ->
                Math.abs(x) + Math.abs(y) + Math.abs(z);
            default ->
                getDistance(x, y, z);
        };
    }

    public double getDistance(double x, double y, double z) {
        return switch (this) {
            case CUBE, CHEBYSHEV ->
                Math.max(Math.max(x, y), z);
            case STAR, MANHATTAN ->
                x + y + z;
            case SPHERE, EUCLIDEAN ->
                Math.sqrt(x * x + y * y + z * z);
        };
    }

    @Deprecated(forRemoval = true)
    public static Collection<BlockPos> getPositions(BlockPos center, Shape shape, int radius) {
        return shape.getBlockPositions(center, radius);
    }

    @Deprecated(forRemoval = true)
    public static Set<Entity> getEntities(Shape shape, Level world, Vec3 center, double radius) {
        return (ObjectOpenHashSet<Entity>) shape.getEntities(world, center, radius);    // Should be safe
    }

    @Deprecated(forRemoval = true)
    public static double getDistance(Shape shape, double xDistance, double yDistance, double zDistance){
        return shape.getDistance(xDistance, yDistance, zDistance);
    }

}
