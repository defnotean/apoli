package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.type.ModifyBlockRenderPowerType;
import io.github.apace100.apoli.power.type.ModifyFluidRenderPowerType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(SectionRenderDispatcher.class)
public class ChunkRendererRegionMixin {

    @Inject(method = "getBlockState", at = @At("HEAD"), cancellable = true)
    private void modifyBlockRender(BlockPos pos, CallbackInfoReturnable<BlockState> cir) {
        Minecraft client = Minecraft.getInstance();
        if(client.level != null && client.player != null) {
            for(ModifyBlockRenderPowerType power : PowerHolderComponent.getPowerTypes(client.player, ModifyBlockRenderPowerType.class)) {
                if(power.doesPrevent(client.level, pos)) {
                    cir.setReturnValue(power.getBlockState());
                    return;
                }
            }
        }
    }

    @Inject(method = "getFluidState", at = @At("HEAD"), cancellable = true)
    private void modifyFluidRender(BlockPos pos, CallbackInfoReturnable<FluidState> cir) {
        Minecraft client = Minecraft.getInstance();
        if(client.level != null && client.player != null) {
            for(ModifyFluidRenderPowerType power : PowerHolderComponent.getPowerTypes(client.player, ModifyFluidRenderPowerType.class)) {
                if(power.doesPrevent(client.level, pos)) {
                    cir.setReturnValue(power.getFluidState());
                    return;
                }
            }
        }
    }
}
