package io.github.apace100.apoli.access;

import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.BlockGetter;

public interface BlockStateCollisionShapeAccess {
    VoxelShape apoli$getOriginalCollisionShape(BlockGetter world, BlockPos pos, CollisionContext context);
}
