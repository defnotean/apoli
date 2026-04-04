package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.type.ModifyExperiencePowerType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ExperienceOrb.class)
public class ExperienceOrbEntityMixin {

    @Shadow
    private int amount;

    @Inject(method = "playerTouch", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/player/Player;experiencePickUpDelay:I", ordinal = 1))
    private void modifyXpAmount(Player player, CallbackInfo ci) {
        this.amount = (int) PowerHolderComponent.modify(player, ModifyExperiencePowerType.class, this.amount);
    }
}
