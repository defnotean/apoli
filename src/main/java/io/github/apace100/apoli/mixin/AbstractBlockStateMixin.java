package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import io.github.apace100.apoli.access.BlockStateCollisionShapeAccess;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.type.PhasingPowerType;
import io.github.apace100.apoli.power.type.PreventBlockSelectionPowerType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class AbstractBlockStateMixin extends StateHolder<Block, BlockState> implements BlockStateCollisionShapeAccess {

    @Shadow
    public abstract Block getBlock();

    @Shadow
    public abstract VoxelShape getCollisionShape(BlockGetter world, BlockPos pos, CollisionContext context);

    @Unique
    private boolean apoli$queryOriginal = false;

    protected AbstractBlockStateMixin(Block owner, Property<?>[] properties, Comparable<?>[] values) {
        super(owner, properties, values);
    }

    // TODO: MC 26.1 renamed getOutlineShape -> getShape
    @ModifyReturnValue(method = "getShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/CollisionContext;)Lnet/minecraft/world/phys/shapes/VoxelShape;", at = @At("RETURN"))
    private VoxelShape apoli$preventBlockSelection(VoxelShape original, BlockGetter blockView, BlockPos blockPos, CollisionContext context) {

        if (context == CollisionContext.empty()) {
            return original;
        }

        else {
            return PreventBlockSelectionPowerType.doesPrevent(context, blockPos)
                ? Shapes.empty()
                : original;
        }

    }

    @ModifyReturnValue(method = "getCollisionShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/CollisionContext;)Lnet/minecraft/world/phys/shapes/VoxelShape;", at = @At("RETURN"))
    private VoxelShape apoli$phaseThroughBlocks(VoxelShape original, BlockGetter blockView, BlockPos blockPos, CollisionContext context) {

        if (context == CollisionContext.empty()) {
            return original;
        }

        else {
            return !apoli$queryOriginal && PhasingPowerType.shouldPhase(context, original, blockPos)
                ? Shapes.empty()
                : original;
        }

    }

    // TODO: MC 26.1 renamed onEntityCollision -> entityInside with new signature including InsideBlockEffectApplier and boolean.
    // Block.onEntityCollision no longer exists. The method and target both need updating.
    @WrapWithCondition(method = "entityInside", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/Block;entityInside(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/InsideBlockEffectApplier;Z)V"))
    private boolean apoli$preventOnEntityCollisionCallWhenPhasing(Block instance, BlockState state, Level world, BlockPos blockPos, Entity entity, net.minecraft.world.entity.InsideBlockEffectApplier applier, boolean bl) {
        return !PowerHolderComponent.hasPowerType(entity, PhasingPowerType.class, p -> p.doesApply(blockPos));
    }

    @Override
    public VoxelShape apoli$getOriginalCollisionShape(BlockGetter world, BlockPos pos, CollisionContext context) {

        this.apoli$queryOriginal = true;
        VoxelShape originalShape = this.getCollisionShape(world, pos, context);

        this.apoli$queryOriginal = false;
        return originalShape;

    }

}
