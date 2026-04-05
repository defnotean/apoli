package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.type.OverrideHudTexturePowerType;
import io.github.apace100.apoli.screen.GameHudRender;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.GuiRenderer;
// TODO: MC 26.1 - LayeredDraw removed
import net.minecraft.client.gui.Gui;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.Identifier;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Comparator;
import java.util.Optional;

@Mixin(Gui.class)
@Environment(EnvType.CLIENT)
public abstract class InGameHudMixin {

    @Shadow @Final private Minecraft client;

    @Shadow protected abstract Player getCameraPlayer();

    // TODO: MC 26.1 - LayeredDraw removed. Find new way to register HUD renders.
    // @Inject(method = "<init>", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/Gui;layeredDrawer:Lnet/minecraft/client/gui/LayeredDraw;", opcode = Opcodes.GETFIELD))
    // private void apoli$renderResourceBars(Minecraft client, CallbackInfo ci, @Local(ordinal = 0) LayeredDraw layeredDrawer) {
    //     for (GameHudRender hudRender : GameHudRender.HUD_RENDERS) {
    //         ((LayeredDrawerAccessor) layeredDrawer).getLayers().add(3, hudRender::render);
    //     }
    // }

    @Unique
    private static Optional<OverrideHudTexturePowerType> apoli$getOverrideHudTexturePower(Player player) {
        return PowerHolderComponent.getPowerTypes(player, OverrideHudTexturePowerType.class)
            .stream()
            .max(Comparator.comparing(OverrideHudTexturePowerType::getPriority));
    }
    
    @WrapOperation(method = "renderArmor", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawGuiTexture(Lnet/minecraft/resources/Identifier;IIII)V", ordinal = 0))
    private static void apoli$overrideFullArmorSprite(GuiRenderer context, Identifier texture, int x, int y, int width, int height, Operation<Void> original, GuiRenderer mContext, Player player) {
        apoli$getOverrideHudTexturePower(player).ifPresentOrElse(
            p -> p.drawTexture(context, texture, x, y, 34, 9, width, height),
            () -> original.call(context, texture, x, y, width, height)
        );
    }

    @WrapOperation(method = "renderArmor", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawGuiTexture(Lnet/minecraft/resources/Identifier;IIII)V", ordinal = 1))
    private static void apoli$overrideHalfArmorSprite(GuiRenderer context, Identifier texture, int x, int y, int width, int height, Operation<Void> original, GuiRenderer mContext, Player player) {
        apoli$getOverrideHudTexturePower(player).ifPresentOrElse(
            p -> p.drawTexture(context, texture, x, y, 25, 9, width, height),
            () -> original.call(context, texture, x, y, width, height)
        );
    }

    @WrapOperation(method = "renderArmor", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawGuiTexture(Lnet/minecraft/resources/Identifier;IIII)V", ordinal = 2))
    private static void apoli$overrideEmptyArmorSprite(GuiRenderer context, Identifier texture, int x, int y, int width, int height, Operation<Void> original, GuiRenderer mContext, Player player) {
        apoli$getOverrideHudTexturePower(player).ifPresentOrElse(
            p -> p.drawTexture(context, texture, x, y, 16, 9, width, height),
            () -> original.call(context, texture, x, y, width, height)
        );
    }

    @WrapOperation(method = "renderFood", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawGuiTexture(Lnet/minecraft/resources/Identifier;IIII)V", ordinal = 0))
    private void apoli$overrideEmptyFoodSprite(GuiRenderer context, Identifier texture, int x, int y, int width, int height, Operation<Void> original, GuiRenderer mContext, Player player) {
        apoli$getOverrideHudTexturePower(player).ifPresentOrElse(
            p -> p.drawTexture(context, texture, x, y, this.getCameraPlayer().hasEffect(MobEffects.HUNGER) ? 133 : 16, 27, width, height),
            () -> original.call(context, texture, x, y, width, height)
        );
    }

    @WrapOperation(method = "renderFood", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawGuiTexture(Lnet/minecraft/resources/Identifier;IIII)V", ordinal = 1))
    private void apoli$overrideFullFoodSprite(GuiRenderer context, Identifier texture, int x, int y, int width, int height, Operation<Void> original, GuiRenderer mContext, Player player) {
        apoli$getOverrideHudTexturePower(player).ifPresentOrElse(
            p -> p.drawTexture(context, texture, x, y, this.getCameraPlayer().hasEffect(MobEffects.HUNGER) ? 88 : 52, 27, width, height),
            () -> original.call(context, texture, x, y, width, height)
        );
    }

    @WrapOperation(method = "renderFood", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawGuiTexture(Lnet/minecraft/resources/Identifier;IIII)V", ordinal = 2))
    private void apoli$overrideHalfFoodSprite(GuiRenderer context, Identifier texture, int x, int y, int width, int height, Operation<Void> original, GuiRenderer mContext, Player player) {
        apoli$getOverrideHudTexturePower(player).ifPresentOrElse(
            p -> p.drawTexture(context, texture, x, y, this.getCameraPlayer().hasEffect(MobEffects.HUNGER) ? 97 : 61, 27, width, height),
            () -> original.call(context, texture, x, y, width, height)
        );
    }

    @WrapOperation(method = "renderStatusBars", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawGuiTexture(Lnet/minecraft/resources/Identifier;IIII)V", ordinal = 0), slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;isSubmergedIn(Lnet/minecraft/tags/TagKey;)Z")))
    private void apoli$overrideBubbleSprite(GuiRenderer context, Identifier texture, int x, int y, int width, int height, Operation<Void> original) {
        apoli$getOverrideHudTexturePower(this.client.player).ifPresentOrElse(
            p -> p.drawTexture(context, texture, x, y, 16, 18, width, height),
            () -> original.call(context, texture, x, y, width, height)
        );
    }

    @WrapOperation(method = "renderStatusBars", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawGuiTexture(Lnet/minecraft/resources/Identifier;IIII)V", ordinal = 1), slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;isSubmergedIn(Lnet/minecraft/tags/TagKey;)Z")))
    private void apoli$overrideBurstingBubbleSprite(GuiRenderer instance, Identifier texture, int x, int y, int width, int height, Operation<Void> original) {
        apoli$getOverrideHudTexturePower(this.client.player).ifPresentOrElse(
            p -> p.drawTexture(instance, texture, x, y, 25, 18, width, height),
            () -> original.call(instance, texture, x, y, width, height)
        );
    }

    @WrapOperation(method = "renderHeart", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawGuiTexture(Lnet/minecraft/resources/Identifier;IIII)V"))
    private void apoli$overrideHeartSprite(GuiRenderer instance, Identifier texture, int x, int y, int width, int height, Operation<Void> original, GuiRenderer context, Gui.HeartType type, int mX, int mY, boolean hardcore, boolean blinking, boolean half) {
        apoli$getOverrideHudTexturePower(this.client.player).ifPresentOrElse(
            p -> p.drawHeartTexture(instance, type, x, y, width, height, hardcore, blinking, half),
            () -> original.call(instance, texture, x, y, width, height)
        );
    }

    @WrapOperation(method = "renderExperienceLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawGuiTexture(Lnet/minecraft/resources/Identifier;IIII)V"))
    private void apoli$overrideBaseExperienceBarSprite(GuiRenderer instance, Identifier texture, int x, int y, int width, int height, Operation<Void> original) {
        apoli$getOverrideHudTexturePower(this.client.player).ifPresentOrElse(
            p -> p.drawTexture(instance, texture, x, y, 0, 64, width, height),
            () -> original.call(instance, texture, x, y, width, height)
        );
    }

    @WrapOperation(method = "renderExperienceLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawGuiTexture(Lnet/minecraft/resources/Identifier;IIIIIIII)V"))
    private void apoli$overrideProgressExperienceBarSprite(GuiRenderer instance, Identifier texture, int i, int j, int k, int l, int x, int y, int width, int height, Operation<Void> original, @Local(ordinal = 1) int experienceProgress) {
        apoli$getOverrideHudTexturePower(this.client.player).ifPresentOrElse(
            p -> p.drawTextureRegion(instance, texture, i, j, k, l, 0, 69, x, y, width, height),
            () -> original.call(instance, texture, i, j, k, l, x, y, width, height)
        );
    }

    @WrapOperation(method = "renderCrosshair", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawGuiTexture(Lnet/minecraft/resources/Identifier;IIII)V", ordinal = 0))
    private void apoli$overrideCrosshairSprite(GuiRenderer instance, Identifier texture, int x, int y, int width, int height, Operation<Void> original) {
        apoli$getOverrideHudTexturePower(this.client.player).ifPresentOrElse(
            p -> p.drawTexture(instance, texture, x, y, 0, 0, width, height),
            () -> original.call(instance, texture, x, y, width, height)
        );
    }

    @WrapOperation(method = "renderCrosshair", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawGuiTexture(Lnet/minecraft/resources/Identifier;IIII)V", ordinal = 0), slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/client/Options;getAttackIndicator()Lnet/minecraft/client/OptionInstance;"), to = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawGuiTexture(Lnet/minecraft/resources/Identifier;IIIIIIII)V")))
    private void apoli$overrideFullCrosshairAttackIndicatorSprite(GuiRenderer instance, Identifier texture, int x, int y, int width, int height, Operation<Void> original) {
        apoli$getOverrideHudTexturePower(this.client.player).ifPresentOrElse(
            p -> p.drawTexture(instance, texture, x, y, 68, 94, width, height),
            () -> original.call(instance, texture, x, y, width, height)
        );
    }

    @WrapOperation(method = "renderCrosshair", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawGuiTexture(Lnet/minecraft/resources/Identifier;IIII)V", ordinal = 1), slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/client/Options;getAttackIndicator()Lnet/minecraft/client/OptionInstance;"), to = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawGuiTexture(Lnet/minecraft/resources/Identifier;IIIIIIII)V")))
    private void apoli$overrideBaseCrosshairAttackIndicatorSprite(GuiRenderer instance, Identifier texture, int x, int y, int width, int height, Operation<Void> original) {
        apoli$getOverrideHudTexturePower(this.client.player).ifPresentOrElse(
            p -> p.drawTexture(instance, texture, x, y, 36, 94, width, height),
            () -> original.call(instance, texture, x, y, width, height)
        );
    }

    @WrapOperation(method = "renderCrosshair", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawGuiTexture(Lnet/minecraft/resources/Identifier;IIIIIIII)V"))
    private void apoli$overrideCrosshairAttackIndicatorProgressSprite(GuiRenderer instance, Identifier texture, int i, int j, int k, int l, int x, int y, int width, int height, Operation<Void> original) {
        apoli$getOverrideHudTexturePower(this.client.player).ifPresentOrElse(
            p -> p.drawTextureRegion(instance, texture, i, j, k, l, 52, 94, x, y, width, height),
            () -> original.call(instance, texture, i, j, k, l, x, y, width, height)
        );
    }

    @WrapOperation(method = "renderJumpMeter", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawGuiTexture(Lnet/minecraft/resources/Identifier;IIII)V"))
    private void apoli$overrideBaseMountJumpBarSprite(GuiRenderer instance, Identifier texture, int x, int y, int width, int height, Operation<Void> original) {
        apoli$getOverrideHudTexturePower(this.client.player).ifPresentOrElse(
            p -> p.drawTexture(instance, texture, x, y, 0, 84, width, height),
            () -> original.call(instance, texture, x, y, width, height)
        );
    }

   @WrapOperation(method = "renderJumpMeter", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawGuiTexture(Lnet/minecraft/resources/Identifier;IIIIIIII)V"))
    private void apoli$overrideMountJumpBarProgressSprite(GuiRenderer instance, Identifier texture, int i, int j, int k, int l, int x, int y, int width, int height, Operation<Void> original) {
       apoli$getOverrideHudTexturePower(this.client.player).ifPresentOrElse(
           p -> p.drawTextureRegion(instance, texture, i, j, k, l, 0, 89, x, y, width, height),
           () -> original.call(instance, texture, i, j, k, l, x, y, width, height)
       );
   }

   @WrapOperation(method = "renderVehicleHealth", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawGuiTexture(Lnet/minecraft/resources/Identifier;IIII)V", ordinal = 0))
    private void apoli$overrideEmptyMountHeartSprite(GuiRenderer instance, Identifier texture, int x, int y, int width, int height, Operation<Void> original) {
        apoli$getOverrideHudTexturePower(this.client.player).ifPresentOrElse(
            p -> p.drawTexture(instance, texture, x, y, 52, 9, width, height),
            () -> original.call(instance, texture, x, y, width, height)
        );
   }

    @WrapOperation(method = "renderVehicleHealth", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawGuiTexture(Lnet/minecraft/resources/Identifier;IIII)V", ordinal = 1))
    private void apoli$overrideFullMountHeartSprite(GuiRenderer instance, Identifier texture, int x, int y, int width, int height, Operation<Void> original) {
        apoli$getOverrideHudTexturePower(this.client.player).ifPresentOrElse(
            p -> p.drawTexture(instance, texture, x, y, 88, 9, width, height),
            () -> original.call(instance, texture, x, y, width, height)
        );
    }

    @WrapOperation(method = "renderVehicleHealth", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawGuiTexture(Lnet/minecraft/resources/Identifier;IIII)V", ordinal = 2))
    private void apoli$overrideHalfMountHeartSprite(GuiRenderer instance, Identifier texture, int x, int y, int width, int height, Operation<Void> original) {
        apoli$getOverrideHudTexturePower(this.client.player).ifPresentOrElse(
            p -> p.drawTexture(instance, texture, x, y, 97, 9, width, height),
            () -> original.call(instance, texture, x, y, width, height)
        );
    }

}
