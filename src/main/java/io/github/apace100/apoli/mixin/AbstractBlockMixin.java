package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.type.ModifyBreakSpeedPowerType;
import io.github.apace100.apoli.util.modifier.Modifier;
import io.github.apace100.apoli.util.modifier.ModifierUtil;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(BlockBehaviour.class)
public abstract class AbstractBlockMixin {

    @ModifyExpressionValue(method = "getDestroyProgress", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getHardness(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)F"))
    private float apoli$modifyBlockHardness(float original, BlockState state, Player player, BlockGetter world, BlockPos pos) {

        List<Modifier> hardnessModifiers = PowerHolderComponent.getPowerTypes(player, ModifyBreakSpeedPowerType.class)
            .stream()
            .filter(p -> p.doesApply(pos))
            .flatMap(p -> p.getHardnessModifiers().stream())
            .toList();

        return (float) Math.max(ModifierUtil.applyModifiers(player, hardnessModifiers, original), -1.0F);

    }

    @ModifyReturnValue(method = "getDestroyProgress", at = @At("RETURN"))
    private float apoli$modifyBlockBreakSpeed(float original, BlockState state, Player player, BlockGetter world, BlockPos pos) {
        return PowerHolderComponent.modify(player, ModifyBreakSpeedPowerType.class, original, mbsp -> mbsp.doesApply(pos));
    }

}
