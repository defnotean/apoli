package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.type.*;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.material.FogType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.DeltaTracker;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.resources.Identifier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;

@Environment(EnvType.CLIENT)
@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    @Shadow
    @Final
    private Camera mainCamera;

    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    private Identifier postEffectId;

    @Shadow
    private boolean effectActive;

    @Shadow
    public abstract void clearPostEffect();

    @Shadow
    protected abstract void setPostEffect(Identifier identifier);

    @Unique
    private Identifier apoli$currentlyLoadedShader;

    @Inject(at = @At("TAIL"), method = "checkEntityPostEffect")
    private void apoli$loadShaderFromPowerOnCameraEntity(Entity entity, CallbackInfo ci) {

        PowerHolderComponent.getPowerTypes(minecraft.getCameraEntity(), ShaderPowerType.class)
            .stream()
            .max(Comparator.comparing(ShaderPowerType::getPriority))
            .ifPresent(p -> {

                Identifier shaderLocation = p.getShaderLocation();

                setPostEffect(shaderLocation);
                apoli$currentlyLoadedShader = shaderLocation;

            });

    }

    @Inject(at = @At("HEAD"), method = "render")
    private void apoli$loadShaderFromPower(DeltaTracker tickCounter, boolean tick, CallbackInfo ci) {

        //  Load a shader from a shader power with a high priority
        PowerHolderComponent.getPowerTypes(minecraft.getCameraEntity(), ShaderPowerType.class)
            .stream()
            .max(Comparator.comparing(ShaderPowerType::getPriority))
            .ifPresent(p -> {
                Identifier shaderLocation = p.getShaderLocation();
                if (shaderLocation != apoli$currentlyLoadedShader) {
                    setPostEffect(shaderLocation);
                    apoli$currentlyLoadedShader = shaderLocation;
                }
            });

        //  Remove the currently loaded shader if the entity doesn't have any shader powers
        if (!PowerHolderComponent.hasPowerType(minecraft.getCameraEntity(), ShaderPowerType.class) && apoli$currentlyLoadedShader != null) {

            clearPostEffect();
            effectActive = false;
            apoli$currentlyLoadedShader = null;

        }

    }

    // TODO: MC 26.1 removed Options.hudHidden field access from render(). Overlay rendering moved to GuiRenderer.
    // OverlayPowerType rendering (below/above HUD) needs to be reimplemented via the new GuiRenderer system.

    @Inject(at = @At("HEAD"), method = "togglePostEffect", cancellable = true)
    private void disableShaderToggle(CallbackInfo ci) {
        PowerHolderComponent.withPowerType(minecraft.getCameraEntity(), ShaderPowerType.class, p -> true, shaderPower -> {
            Identifier shaderLoc = shaderPower.getShaderLocation();
            if(!shaderPower.isToggleable() && apoli$currentlyLoadedShader == shaderLoc) {
                ci.cancel();
            }
        });
    }

    // NightVisionPower
    @WrapMethod(method = "getNightVisionScale")
    private static float apoli$modifyNightVisionStrength(LivingEntity entity, float tickDelta, Operation<Float> original) {
        return PowerHolderComponent.getPowerTypes(entity, NightVisionPowerType.class)
            .stream()
            .map(NightVisionPowerType::getStrength)
            .max(Float::compareTo)
            .orElseGet(() -> original.call(entity, tickDelta));
    }

    // TODO: MC 26.1 removed getFov() from GameRenderer. FOV modification for ModifyCameraSubmersionTypePowerType
    // needs to be reimplemented via the new extract/update pipeline.

    @Unique
    private final HashMap<BlockPos, BlockState> savedStates = new HashMap<>();

    // PHASING: remove_blocks
    @Inject(at = @At(value = "HEAD"), method = "render")
    private void beforeRender(DeltaTracker tickCounter, boolean tick, CallbackInfo ci) {
        List<PhasingPowerType> phasings = PowerHolderComponent.getPowerTypes(mainCamera.entity(), PhasingPowerType.class);
        if (phasings.stream().anyMatch(pp -> pp.getRenderType() == PhasingPowerType.RenderType.REMOVE_BLOCKS)) {
            float view = phasings.stream().filter(pp -> pp.getRenderType() == PhasingPowerType.RenderType.REMOVE_BLOCKS).map(PhasingPowerType::getViewDistance).min(Float::compareTo).get();
            Set<BlockPos> eyePositions = getEyePos(0.25F, 0.05F, 0.25F);
            Set<BlockPos> noLongerEyePositions = new HashSet<>();
            for (BlockPos p : savedStates.keySet()) {
                if (!eyePositions.contains(p)) {
                    noLongerEyePositions.add(p);
                }
            }
            for (BlockPos eyePosition : noLongerEyePositions) {
                BlockState state = savedStates.get(eyePosition);
                minecraft.level.setBlockAndUpdate(eyePosition, state);
                savedStates.remove(eyePosition);
            }
            for (BlockPos p : eyePositions) {
                BlockState stateAtP = minecraft.level.getBlockState(p);
                if (!savedStates.containsKey(p) && !minecraft.level.isEmptyBlock(p) && !(stateAtP.getBlock() instanceof LiquidBlock)) {
                    savedStates.put(p, stateAtP);
                    minecraft.level.setBlockAndUpdate(p, Blocks.AIR.defaultBlockState());
                }
            }
        } else if (savedStates.size() > 0) {
            Set<BlockPos> noLongerEyePositions = new HashSet<>(savedStates.keySet());
            for (BlockPos eyePosition : noLongerEyePositions) {
                BlockState state = savedStates.get(eyePosition);
                minecraft.level.setBlockAndUpdate(eyePosition, state);
                savedStates.remove(eyePosition);
            }
        }
    }

    // PHASING: In MC 26.1 Camera.update() takes only a DeltaTracker - third person prevention handled elsewhere
    // TODO: Re-implement phasing third-person prevention using the new Camera API (setCameraType or similar).

    @Unique
    private Set<BlockPos> getEyePos(float rangeX, float rangeY, float rangeZ) {
        Vec3 pos = mainCamera.entity().getEyePosition();
        AABB cameraBox = new AABB(pos, pos);
        cameraBox = cameraBox.inflate(rangeX, rangeY, rangeZ);
        HashSet<BlockPos> set = new HashSet<>();
        BlockPos.betweenClosedStream(cameraBox).forEach(p -> set.add(p.immutable()));
        return set;
    }

    // TODO: MC 26.1 removed pick() from GameRenderer. Entity selection prevention for
    // PreventEntitySelectionPowerType needs to be reimplemented elsewhere.

}
