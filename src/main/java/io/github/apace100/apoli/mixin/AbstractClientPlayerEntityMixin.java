package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.authlib.GameProfile;
import io.github.apace100.apoli.component.PowerHolderComponent;

import io.github.apace100.apoli.power.type.ModifyFovPowerType;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(AbstractClientPlayer.class)
public abstract class AbstractClientPlayerEntityMixin extends Player {

    private AbstractClientPlayerEntityMixin(Level world, GameProfile gameProfile) {
        super(world, gameProfile);
    }

    @ModifyReturnValue(method = "getFieldOfViewModifier", at = @At(value = "RETURN", ordinal = 0))
    private float apoli$modifySpyglassFov(float original) {
        return PowerHolderComponent.modify(this, ModifyFovPowerType.class, original);
    }

    @WrapOperation(method = "getFieldOfViewModifier", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;lerp(FFF)F"))
    private float apoli$modifyFov(float delta, float start, float end, Operation<Float> original) {

        List<ModifyFovPowerType> mfps = PowerHolderComponent.getPowerTypes(this, ModifyFovPowerType.class);
        boolean affectedByFovEffectScale = mfps.isEmpty() || mfps
            .stream()
            .anyMatch(ModifyFovPowerType::isAffectedByFovEffectScale);

        float newEnd = PowerHolderComponent.modify(this, ModifyFovPowerType.class, end);
        float newDelta = affectedByFovEffectScale ? delta : start;

        return original.call(newDelta, start, newEnd);

    }

}
