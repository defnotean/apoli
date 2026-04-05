package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.type.OverrideHudTexturePowerType;
import io.github.apace100.apoli.screen.GameHudRender;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Gui;
import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Comparator;
import java.util.Optional;

@Mixin(Gui.class)
@Environment(EnvType.CLIENT)
public abstract class InGameHudMixin {

    // MC 26.1: field is 'minecraft' not 'client' on Gui
    @Shadow @Final private Minecraft minecraft;

    // MC 26.1: getCameraPlayer() is private on Gui. Not currently used, shadow removed.

    @Unique
    private static Optional<OverrideHudTexturePowerType> apoli$getOverrideHudTexturePower(Player player) {
        return PowerHolderComponent.getPowerTypes(player, OverrideHudTexturePowerType.class)
            .stream()
            .max(Comparator.comparing(OverrideHudTexturePowerType::getPriority));
    }

    // MC 26.1: The HUD rendering system has been completely restructured.
    // The old methods (renderArmor, renderFood, renderStatusBars, renderCrosshair, etc.)
    // no longer exist. The Gui class now uses an extract-render state pattern via
    // GuiGraphicsExtractor. HUD texture overrides need to be reimplemented using the
    // new blitSprite-based rendering system.
    //
    // The extractHeart method is used for heart rendering, and extractRenderState
    // drives the overall HUD extraction.
    //
    // TODO: Reimplement HUD texture overrides for the new GuiGraphicsExtractor-based system.

    @WrapOperation(method = "extractHeart", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V"))
    private void apoli$overrideHeartSprite(GuiGraphicsExtractor extractor, com.mojang.blaze3d.pipeline.RenderPipeline pipeline, Identifier texture, int x, int y, int width, int height, Operation<Void> original, GuiGraphicsExtractor mExtractor, Gui.HeartType type, int mX, int mY, boolean hardcore, boolean blinking, boolean half) {
        Optional<OverrideHudTexturePowerType> power = apoli$getOverrideHudTexturePower(this.minecraft.player);
        if (power.isPresent()) {
            power.get().drawHeartTexture(extractor, type, x, y, width, height, hardcore, blinking, half);
        } else {
            original.call(extractor, pipeline, texture, x, y, width, height);
        }
    }

}
