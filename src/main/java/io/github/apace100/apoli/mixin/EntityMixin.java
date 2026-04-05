package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.access.*;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.data.ApoliDataHandlers;
import io.github.apace100.apoli.power.type.*;
import io.github.apace100.apoli.util.ArmPoseReference;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.TagKey;
import net.minecraft.world.scores.Team;
import net.minecraft.network.chat.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Mixin(Entity.class)
public abstract class EntityMixin implements MovingEntity, ModifiedPoseHolder, CustomLeashable {

    @Shadow
    private boolean onGround;

    @Shadow public abstract Vec3 getPos();

    @Shadow public abstract double getX();

    @Shadow public abstract double getY();

    @Shadow public abstract double getZ();

    @Shadow public abstract Level level();

    @Shadow @Final protected SynchedEntityData entityData;

    @Shadow @Final private Set<String> tags;

    @Shadow public abstract Component getName();

    @Shadow public abstract SynchedEntityData getEntityData();

    @Shadow public abstract void setPose(Pose pose);

    @Shadow public abstract Pose getPose();

    @Shadow public abstract boolean isSwimming();

    @Shadow public abstract EntityType<?> getType();

    @ModifyReturnValue(method = "fireImmune", at = @At("RETURN"))
    private boolean apoli$makeFullyFireImmune(boolean original) {
        return original
            || PowerHolderComponent.hasPowerType((Entity) (Object) this, FireImmunityPowerType.class);
    }

    @ModifyReturnValue(method = "isInWater", at = @At("RETURN"))
    private boolean apoli$makeEntitiesIgnoreWater(boolean original) {

        if (!(this instanceof WaterMovingEntity waterMovingEntity)) {
            return original;
        }

        return original
            && !(waterMovingEntity.apoli$isInMovementPhase() && PowerHolderComponent.hasPowerType((Entity) (Object) this, IgnoreWaterPowerType.class));

    }

    @Inject(method = "fall", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/Block;onLandedUpon(Lnet/minecraft/world/Level;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/Entity;F)V"))
    private void invokeActionOnLand(CallbackInfo ci) {
        PowerHolderComponent.withPowerTypes((Entity) (Object) this, ActionOnLandPowerType.class, p -> true, ActionOnLandPowerType::executeAction);
    }

    @ModifyReturnValue(method = "isInvulnerableTo", at = @At("RETURN"))
    private boolean apoli$makeEntitiesInvulnerable(boolean original, DamageSource source) {
        return original
            || PowerHolderComponent.hasPowerType((Entity) (Object) this, InvulnerabilityPowerType.class, p -> p.doesApply(source));
    }

    @ModifyExpressionValue(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;isWet()Z"))
    private boolean apoli$preventExtinguishingFromPowerSwimming(boolean original) {
        return original
            && !(this.isSwimming() && PowerHolderComponent.hasPowerType((Entity) (Object) this, SwimmingPowerType.class));
    }

    @ModifyReturnValue(method = "isInvisible", at = @At("RETURN"))
    private boolean apoli$invisibility(boolean original) {
        return original
            || PowerHolderComponent.hasPowerType((Entity) (Object) this, InvisibilityPowerType.class);
    }

    @WrapOperation(method = "isInvisibleTo", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;isInvisible()Z"))
    private boolean apoli$specificallyInvisibleTo(Entity entity, Operation<Boolean> original, Player viewer) {

        List<InvisibilityPowerType> invisibilityPowers = PowerHolderComponent.getPowerTypes(entity, InvisibilityPowerType.class, true);
        if (viewer == null || invisibilityPowers.isEmpty()) {
            return original.call(entity);
        }

        return invisibilityPowers
            .stream()
            .anyMatch(p -> p.isActive() && p.doesApply(viewer));

    }

    //  TODO: Use MixinExtras' @WrapMethod from its new beta releases -eggohito
    @Inject(method = "moveTowardsClosestSpace", at = @At(value = "NEW", target = "()Lnet/minecraft/core/BlockPos$Mutable;"), cancellable = true)
    protected void apoli$ignorePhasingEntities(double x, double y, double z, CallbackInfo ci, @Local BlockPos pos) {

        if (PowerHolderComponent.hasPowerType((Entity) (Object) this, PhasingPowerType.class, p -> p.doesApply(pos))) {
            ci.cancel();
        }

    }

    @Redirect(method = "isInWall", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getCollisionShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/phys/shapes/VoxelShape;"))
    private VoxelShape preventPhasingSuffocation(BlockState state, BlockGetter world, BlockPos pos) {
        return state.getCollisionShape(world, pos, CollisionContext.of((Entity)(Object)this));
    }

    @ModifyVariable(method = "move", at = @At("HEAD"), argsOnly = true)
    private Vec3 modifyMovementVelocity(Vec3 original, MoverType movementType) {

        if (movementType != MoverType.SELF) {
            return original;
        }

        return new Vec3(
            PowerHolderComponent.modify((Entity)(Object) this, ModifyVelocityPowerType.class, original.x, p -> p.doesApply(Direction.Axis.X), p -> {}),
            PowerHolderComponent.modify((Entity)(Object) this, ModifyVelocityPowerType.class, original.y, p -> p.doesApply(Direction.Axis.Y), p -> {}),
            PowerHolderComponent.modify((Entity)(Object) this, ModifyVelocityPowerType.class, original.z, p -> p.doesApply(Direction.Axis.Z), p -> {})
        );

    }

    @Inject(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getLandingPos()Lnet/minecraft/core/BlockPos;"))
    private void forceGrounded(MoverType movementType, Vec3 movement, CallbackInfo ci) {
        if(PowerHolderComponent.hasPowerType((Entity)(Object)this, GroundedPowerType.class)) {
            this.onGround = true;
        }
    }

    @Environment(EnvType.CLIENT)
    @ModifyReturnValue(method = "getTeamColor", at = @At("RETURN"))
    private int apoli$modifyGlowingColorFromPower(int original) {

        //  region Advised by @EdwinMindcraft: a solution for making the hook limited to LevelRenderer ONLY. Uncomment the code in this region when run into unexpected calls to Entity#getTeamColorValue to fix the problem.
//        StackWalker walker = StackWalker.getInstance(Set.of(StackWalker.Option.RETAIN_CLASS_REFERENCE), 2);
//        boolean calledByWorldRenderer = walker.walk(stackFrameStream -> stackFrameStream
//            .map(StackWalker.StackFrame::getDeclaringClass)
//            .anyMatch(cls -> cls == LevelRenderer.class));
//
//        if (!calledByWorldRenderer) {
//            return original;
//        }
        //  endregion

        Entity cameraEntity = Minecraft.getInstance().getCameraEntity();
        Entity renderedEntity = (Entity) (Object) this;

        Team team = renderedEntity.getTeam();

        boolean hasTeamColor = team != null && team.getColor().getColorValue() != null;
        int colorAmount = 0;

        float red = 0.0f;
        float green = 0.0f;
        float blue = 0.0f;

        for (EntityGlowPowerType entityGlowPower : PowerHolderComponent.getPowerTypes(cameraEntity, EntityGlowPowerType.class)) {

            if ((hasTeamColor && entityGlowPower.usesTeams()) || !entityGlowPower.doesApply(renderedEntity)) {
                continue;
            }

            red += entityGlowPower.getRed();
            green += entityGlowPower.getGreen();
            blue += entityGlowPower.getBlue();

            colorAmount++;

        }

        for (SelfGlowPowerType selfGlowPower : PowerHolderComponent.getPowerTypes(renderedEntity, SelfGlowPowerType.class)) {

            if ((hasTeamColor && selfGlowPower.usesTeams()) || !selfGlowPower.doesApply(cameraEntity)) {
                continue;
            }

            red += selfGlowPower.getRed();
            green += selfGlowPower.getGreen();
            blue += selfGlowPower.getBlue();

            colorAmount++;

        }

        return colorAmount > 0
            ? Mth.packRgb(red / colorAmount, green / colorAmount, blue / colorAmount)
            : original;

    }

    @ModifyExpressionValue(method = "push", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;isConnectedThroughVehicle(Lnet/minecraft/world/entity/Entity;)Z"))
    private boolean apoli$preventEntityPushing(boolean original, Entity fromEntity) {
        return original || PreventEntityCollisionPowerType.doesApply(fromEntity, (Entity) (Object) this);
    }

    @ModifyReturnValue(method = "collidesWith", at = @At("RETURN"))
    private boolean apoli$preventEntityCollision(boolean original, Entity other) {
        return !PreventEntityCollisionPowerType.doesApply((Entity) (Object) this, other) && original;
    }

    @Unique
    private boolean apoli$movingHorizontally;

    @Unique
    private boolean apoli$movingVertically;

    @Unique
    private double apoli$horizontalMovementValue;

    @Unique
    private double apoli$verticalMovementValue;

    @Unique
    private Vec3 apoli$prevPos;

    @Override
    public boolean apoli$isMovingHorizontally() {
        return apoli$movingHorizontally;
    }

    @Override
    public boolean apoli$isMovingVertically() {
        return apoli$movingVertically;
    }

    @Override
    public double apoli$getHorizontalMovementValue() {
        return apoli$horizontalMovementValue;
    }

    @Override
    public double apoli$getVerticalMovementValue() {
        return apoli$verticalMovementValue;
    }

    @Override
    public boolean apoli$isMoving() {
        return apoli$movingHorizontally || apoli$movingVertically;
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void apoli$resetMovingFlags(CallbackInfo ci) {
        this.apoli$movingHorizontally = false;
        this.apoli$movingVertically = false;
    }

    @Inject(method = "baseTick", at = @At("TAIL"))
    private void apoli$setMovingFlags(CallbackInfo ci) {

        if (apoli$prevPos == null) {
            this.apoli$prevPos = this.position();
            return;
        }

        double dx = apoli$prevPos.x - this.x();
        double dy = apoli$prevPos.y - this.y();
        double dz = apoli$prevPos.z - this.z();

        this.apoli$horizontalMovementValue = Math.sqrt(dx * dx + dz * dz);
        this.apoli$verticalMovementValue = Math.sqrt(dy * dy);

        this.apoli$prevPos = this.position();

        if (this.apoli$horizontalMovementValue >= 0.01) {
            this.apoli$movingHorizontally = true;
        }

        if (this.apoli$verticalMovementValue >= 0.01) {
            this.apoli$movingVertically = true;
        }

    }

    @ModifyReturnValue(method = "getType", at = @At("RETURN"))
    private EntityType<?> apoli$modifyTypeTag(EntityType<?> original) {

        if (original instanceof EntityLinkedType linkedType) {
            linkedType.apoli$setEntity((Entity) (Object) this);
        }

        return original;

    }

    @WrapOperation(method = "causeFallDamage", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/EntityType;is(Lnet/minecraft/tags/TagKey;)Z"))
    private boolean apoli$fixEntityTypeCalls(EntityType<?> instance, TagKey<EntityType<?>> tag, Operation<Boolean> original) {
        return this.getType().is(tag);
    }

    @Unique
    private static final EntityDataAccessor<Set<String>> COMMAND_TAGS = SynchedEntityData.defineId(Entity.class, ApoliDataHandlers.STRING_SET);

    @Unique
    private boolean apoli$hasCommandTagsTracker = true;

    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;defineSynchedData(Lnet/minecraft/network/syncher/SynchedEntityData$Builder;)V"))
    private void apoli$registerCommandTagsDataTracker(EntityType<?> type, Level world, CallbackInfo ci, @Local SynchedEntityData.Builder builder) {

        try {
            builder.add(COMMAND_TAGS, Set.of());
        }

        catch (Exception e) {
            Apoli.LOGGER.warn("Couldn't register data tracker for command tags for entity {}:", this.getName().getString(), e);
            this.apoli$hasCommandTagsTracker = false;
        }

    }

    @ModifyReturnValue(method = "addTag", at = @At("RETURN"))
    private boolean apoli$trackAddedCommandTag(boolean original) {

        if (original && apoli$hasCommandTagsTracker) {
            this.getEntityData().set(COMMAND_TAGS, Set.copyOf(this.tags));
        }

        return original;

    }

    @ModifyReturnValue(method = "removeTag", at = @At("RETURN"))
    private boolean apoli$trackRemovedCommandTag(boolean original) {

        if (original && apoli$hasCommandTagsTracker) {
            this.getEntityData().set(COMMAND_TAGS, Set.copyOf(this.tags));
        }

        return original;

    }

    @ModifyReturnValue(method = "getTags", at = @At("RETURN"))
    private Set<String> apoli$queryTrackedCommandTags(Set<String> original) {
        return apoli$hasCommandTagsTracker
            ? this.getEntityData().get(COMMAND_TAGS)
            : original;
    }

    @Inject(method = "load", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;readAdditionalSaveData(Lnet/minecraft/nbt/CompoundTag;)V"))
    private void apoli$trackCommandTagsFromNbt(CompoundTag nbt, CallbackInfo ci) {

        if (apoli$hasCommandTagsTracker) {
            this.getEntityData().set(COMMAND_TAGS, Set.copyOf(this.tags));
        }

    }

    @Redirect(method = "save", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/Entity;tags:Ljava/util/Set;"))
    private Set<String> apoli$overrideCommandTagsFieldAccess(Entity entity) {
        return entity.getTags();
    }

    @Unique
    private Pose apoli$previousEntityPose;

    @Unique
    private Pose apoli$modifiedEntityPose;

    @Unique
    private ArmPoseReference apoli$modifiedArmPose;

    @Override
    public Optional<Pose> apoli$getModifiedEntityPose() {
        return Optional.ofNullable(apoli$modifiedEntityPose);
    }

    @Override
    public void apoli$setModifiedEntityPose(Pose entityPose) {
        this.apoli$modifiedEntityPose = entityPose;
    }

    @Override
    public Optional<ArmPoseReference> apoli$getModifiedArmPose() {
        return Optional.ofNullable(apoli$modifiedArmPose);
    }

    @Override
    public void apoli$setModifiedArmPose(ArmPoseReference armPose) {
        this.apoli$modifiedArmPose = armPose;
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void apoli$overridePose(CallbackInfo ci) {
        PowerHolderComponent.getPowerTypes((Entity) (Object) this, PosePowerType.class)
            .stream()
            .max(Comparator.comparing(PosePowerType::getPriority))
            .ifPresentOrElse(
                posePower -> {

                    if (!((Entity) (Object) this instanceof Player) && apoli$previousEntityPose == null) {
                        this.apoli$previousEntityPose = this.getPose();
                    }

                    Optional<Pose> replacementEntityPose = posePower.getEntityPose();

                    this.apoli$setModifiedEntityPose(replacementEntityPose.orElse(null));
                    this.apoli$setModifiedArmPose(posePower.getArmPose().orElse(null));

					replacementEntityPose.ifPresent(this::setPose);

                },
                () -> {

                    this.apoli$setModifiedEntityPose(null);
                    this.apoli$setModifiedArmPose(null);

                    if (apoli$previousEntityPose != null) {
                        this.setPose(apoli$previousEntityPose);
                    }

                    this.apoli$previousEntityPose = null;

                }
            );
    }

    @Unique
    private boolean apoli$customLeashed;

    @Override
    public boolean apoli$isCustomLeashed() {
        return apoli$customLeashed;
    }

    @Override
    public void apoli$setCustomLeashed(boolean customLeashed) {
        this.apoli$customLeashed = customLeashed;
    }

}
