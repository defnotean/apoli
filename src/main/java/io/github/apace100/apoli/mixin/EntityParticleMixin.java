package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.type.ParticlePowerType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.*;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(LivingEntity.class)
public abstract class EntityParticleMixin extends Entity {

    @Shadow public abstract EntityDimensions getDimensions(Pose pose);

    public EntityParticleMixin(EntityType<?> type, Level world) {
        super(type, world);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void apoli$emitParticles(CallbackInfo ci) {

        LocalPlayer player = Minecraft.getInstance().player;
        boolean inFirstPerson = Minecraft.getInstance().options.getPerspective().isFirstPerson();

        if (player == null) {
            return;
        }

        double velocityX;
        double velocityY;
        double velocityZ;

        for (ParticlePowerType particlePower : PowerHolderComponent.getPowerTypes(this, ParticlePowerType.class)) {

            if (!particlePower.doesApply(player, inFirstPerson)) {
                continue;
            }

            Vec3 spread = particlePower
                .getSpread()
                .multiply(this.getWidth(), this.getEyeHeight(this.getPose()), this.getWidth());
            Vec3 particlePos = this
                .position()
                .add(particlePower.getOffsetX(), particlePower.getOffsetY(), particlePower.getOffsetZ());

            if (particlePower.getCount() == 0) {

                velocityX = spread.getX() * particlePower.getSpeed();
                velocityY = spread.getY() * particlePower.getSpeed();
                velocityZ = spread.getZ() * particlePower.getSpeed();

                this.level().addParticle(particlePower.getParticle(), particlePower.shouldForce(), particlePos.getX(), particlePos.getY(), particlePos.getZ(), velocityX, velocityY, velocityZ);

            } else {

                for (int i = 0; i < particlePower.getCount(); i++) {

                    Vec3 newSpread = spread.multiply(this.random.nextGaussian(), this.random.nextGaussian(), this.random.nextGaussian());
                    Vec3 newParticlePos = particlePos.add(newSpread);

                    velocityX = (2.0 * this.random.nextDouble() - 1.0) * particlePower.getSpeed();
                    velocityY = (2.0 * this.random.nextDouble() - 1.0) * particlePower.getSpeed();
                    velocityZ = (2.0 * this.random.nextDouble() - 1.0) * particlePower.getSpeed();

                    this.level().addParticle(particlePower.getParticle(), particlePower.shouldForce(), newParticlePos.getX(), newParticlePos.getY(), newParticlePos.getZ(), velocityX, velocityY, velocityZ);

                }

            }

        }

    }

}
