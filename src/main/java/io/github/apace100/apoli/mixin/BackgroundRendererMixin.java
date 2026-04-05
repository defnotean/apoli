package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.type.ModifyCameraSubmersionTypePowerType;
import io.github.apace100.apoli.power.type.NightVisionPowerType;
import io.github.apace100.apoli.power.type.PhasingPowerType;
import io.github.apace100.apoli.util.MiscUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.level.material.FogType;
import net.minecraft.client.renderer.fog.FogRenderer;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(FogRenderer.class)
@Environment(EnvType.CLIENT)
public abstract class BackgroundRendererMixin {

    @ModifyExpressionValue(method = "setupFog", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/fog/FogRenderer;getFogType(Lnet/minecraft/client/Camera;)Lnet/minecraft/world/level/material/FogType;"))
    private FogType apoli$modifyCameraSubmersionType(FogType original, Camera camera) {
        return PowerHolderComponent.getPowerTypes(camera.getFocusedEntity(), ModifyCameraSubmersionTypePowerType.class, true)
            .stream()
            .filter(p -> p.doesModify(original) && p.isActive())
            .findFirst()
            .map(ModifyCameraSubmersionTypePowerType::getNewType)
            .orElse(original);
    }

    @Inject(method = "setupFog", at = @At("RETURN"))
    private void apoli$modifyFogForPhasing(Camera camera, int viewDistance, DeltaTracker deltaTracker, float skyDarkness, ClientLevel world, CallbackInfoReturnable<FogData> cir) {
        if (camera.getFocusedEntity() instanceof LivingEntity living) {
            List<PhasingPowerType> phasings = PowerHolderComponent.getPowerTypes(living, PhasingPowerType.class);
            if (phasings.stream().anyMatch(pp -> pp.getRenderType() == PhasingPowerType.RenderType.BLINDNESS)) {
                if (MiscUtil.getInWallBlockState(living) != null) {
                    FogData fogData = cir.getReturnValue();
                    float view = phasings.stream()
                        .filter(pp -> pp.getRenderType() == PhasingPowerType.RenderType.BLINDNESS)
                        .map(PhasingPowerType::getViewDistance)
                        .min(Float::compareTo)
                        .get();
                    fogData.environmentalStart = view * 0.25f;
                    fogData.environmentalEnd = view;
                    fogData.renderDistanceStart = 0.0f;
                    fogData.renderDistanceEnd = view * 0.8f;
                    fogData.color.set(0f, 0f, 0f, 1f);
                }
            }
        }
    }

}
