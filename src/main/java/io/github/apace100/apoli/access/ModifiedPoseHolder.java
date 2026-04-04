package io.github.apace100.apoli.access;

import io.github.apace100.apoli.util.ArmPoseReference;
import net.minecraft.world.entity.Pose;

import java.util.Optional;

public interface ModifiedPoseHolder {

    Optional<Pose> apoli$getModifiedEntityPose();
    void apoli$setModifiedEntityPose(Pose entityPose);

    Optional<ArmPoseReference> apoli$getModifiedArmPose();
    void apoli$setModifiedArmPose(ArmPoseReference armPose);

}
