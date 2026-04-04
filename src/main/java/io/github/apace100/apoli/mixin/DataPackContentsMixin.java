package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.power.PowerManager;
import io.github.apace100.apoli.power.type.RecipePowerType;
import net.minecraft.server.ReloadableServerResources;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ReloadableServerResources.class)
public abstract class DataPackContentsMixin {

	@Inject(method = "refresh", at = @At("HEAD"))
	private void onRefresh(CallbackInfo ci) {
		PowerManager.validate();
		RecipePowerType.registerPowerRecipes((ReloadableServerResources) (Object) this);
	}

}
