package io.github.apace100.apoli.mixin.power.type;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.type.ModifyHarvestPowerType;
import io.github.apace100.apoli.util.SavedBlockPosition;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureElement;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public abstract class ModifyHarvestPowerTypeMixin {

	@Mixin(BlockBehaviour.class)
	public abstract static class BlockBreakingDeltaProxy implements FeatureElement {

		@WrapOperation(method = "getDestroyProgress", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;canHarvest(Lnet/minecraft/world/level/block/state/BlockState;)Z"))
		private boolean apoli$modifyHarvest(Player player, BlockState state, Operation<Boolean> original, BlockState mState, Player mPlayer, BlockGetter world, BlockPos pos) {
			return PowerHolderComponent.getPowerTypes(player, ModifyHarvestPowerType.class)
				.stream()
				.filter(powerType -> powerType.doesApply(world, pos))
				.max(ModifyHarvestPowerType::compareTo)
				.map(ModifyHarvestPowerType::isAllowed)
				.orElseGet(() -> original.call(player, state));
		}

	}

	@Mixin(ServerPlayerGameMode.class)
	public abstract static class HarvestabilityProxy {

		@Shadow
		protected ServerLevel world;

		@Shadow
		@Final
		protected ServerPlayer player;

		@Inject(method = "destroyBlock", at = @At("HEAD"))
		private void apoli$cacheBreakingBlock(BlockPos pos, CallbackInfoReturnable<Boolean> cir, @Share(value = "breakingBlock", namespace = Apoli.MODID) LocalRef<SavedBlockPosition> breakingBlockRef) {
			breakingBlockRef.set(new SavedBlockPosition(this.world, pos));
		}

		@WrapOperation(method = "destroyBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayer;canHarvest(Lnet/minecraft/world/level/block/state/BlockState;)Z"))
		private boolean apoli$modifyHarvest(ServerPlayer player, BlockState state, Operation<Boolean> original, @Share(value = "breakingBlock", namespace = Apoli.MODID) LocalRef<SavedBlockPosition> breakingBlockRef, @Share(value = "modifiedHarvest", namespace = Apoli.MODID) LocalBooleanRef modifiedHarvestRef) {

			boolean result = PowerHolderComponent.getPowerTypes(this.player, ModifyHarvestPowerType.class)
				.stream()
				.filter(powerType -> powerType.doesApply(breakingBlockRef.get()))
				.max(ModifyHarvestPowerType::compareTo)
				.map(ModifyHarvestPowerType::isAllowed)
				.orElseGet(() -> original.call(player, state));

			modifiedHarvestRef.set(result);
			return result;

		}

	}

}
