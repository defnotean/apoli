package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import io.github.apace100.apoli.power.type.ActionOnBlockUsePowerType;
import io.github.apace100.apoli.power.type.ActiveInteractionPowerType;
import io.github.apace100.apoli.power.type.PreventBlockUsePowerType;
import io.github.apace100.apoli.power.type.Prioritized;
import io.github.apace100.apoli.util.ActionResultUtil;
import io.github.apace100.apoli.util.BlockUsagePhase;
import io.github.apace100.apoli.util.PriorityPhase;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Slice;

import java.util.List;

@Mixin(MultiPlayerGameMode.class)
public abstract class ClientPlayerInteractionManagerMixin {

    @WrapOperation(method = "useItemOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;onUse(Lnet/minecraft/world/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/phys/BlockHitResult;)Lnet/minecraft/world/InteractionResult;"))
    private InteractionResult apoli$beforeUseBlock(BlockState state, Level world, Player player, BlockHitResult hitResult, Operation<InteractionResult> original, LocalPlayer mPlayer, InteractionHand mHand, @Share("zeroPriority$useBlock") LocalRef<InteractionResult> zeroPriority$useBlockRef) {

        ItemStack stackInHand = player.getItemInHand(mHand);
        BlockUsagePhase usePhase = BlockUsagePhase.BLOCK;

        if (PreventBlockUsePowerType.doesPrevent(player, usePhase, hitResult, stackInHand, mHand)) {
            return InteractionResult.FAIL;
        }

        Prioritized.CallInstance<ActiveInteractionPowerType> aipci = new Prioritized.CallInstance<>();
        aipci.add(player, ActionOnBlockUsePowerType.class, p -> p.shouldExecute(usePhase, PriorityPhase.BEFORE, hitResult, mHand, stackInHand));

        for (int i = aipci.getMaxPriority(); i >= aipci.getMinPriority(); i--) {

            if (!aipci.hasPowerTypes(i)) {
                continue;
            }

            List<ActiveInteractionPowerType> aips = aipci.getPowerTypes(i);
            InteractionResult previousResult = InteractionResult.PASS;

            for (ActiveInteractionPowerType aip : aips) {

                InteractionResult currentResult = aip instanceof ActionOnBlockUsePowerType aobup
                    ? aobup.executeAction(hitResult, mHand)
                    : InteractionResult.PASS;

                if (ActionResultUtil.shouldOverride(previousResult, currentResult)) {
                    previousResult = currentResult;
                }

            }

            if (i == 0) {
                zeroPriority$useBlockRef.set(previousResult);
                continue;
            }

            if (previousResult == InteractionResult.PASS) {
                continue;
            }

            if (previousResult instanceof InteractionResult.Success) {
                player.swing(mHand);
            }

            return previousResult;

        }

        return original.call(state, world, player, hitResult);

    }

    @ModifyReturnValue(method = "useItemOn", at = @At(value = "RETURN", ordinal = 0), slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/world/InteractionResult;isAccepted()Z")))
    private InteractionResult apoli$afterUseBlock(InteractionResult original, LocalPlayer player, InteractionHand hand, BlockHitResult hitResult, @Share("zeroPriority$useBlock") LocalRef<InteractionResult> zeroPriority$useBlockRef) {

        ItemStack stackInHand = player.getItemInHand(hand);

        InteractionResult zeroPriority$useBlock = zeroPriority$useBlockRef.get();
        InteractionResult newResult = InteractionResult.PASS;

        if (zeroPriority$useBlock != null && zeroPriority$useBlock != InteractionResult.PASS) {
            newResult = zeroPriority$useBlock;
        }

        else if (original == InteractionResult.PASS) {

            Prioritized.CallInstance<ActiveInteractionPowerType> aipci = new Prioritized.CallInstance<>();
            aipci.add(player, ActionOnBlockUsePowerType.class, p -> p.shouldExecute(BlockUsagePhase.BLOCK, PriorityPhase.AFTER, hitResult, hand, stackInHand));

            for (int i = aipci.getMaxPriority(); i >= aipci.getMinPriority(); i--) {

                if (!aipci.hasPowerTypes(i)) {
                    continue;
                }

                List<ActiveInteractionPowerType> aips = aipci.getPowerTypes(i);
                InteractionResult previousResult = InteractionResult.PASS;

                for (ActiveInteractionPowerType aip : aips) {

                    InteractionResult currentResult = aip instanceof ActionOnBlockUsePowerType aobup
                        ? aobup.executeAction(hitResult, hand)
                        : InteractionResult.PASS;

                    if (ActionResultUtil.shouldOverride(previousResult, currentResult)) {
                        previousResult = currentResult;
                    }

                }

                if (previousResult != InteractionResult.PASS) {
                    newResult = previousResult;
                    break;
                }

            }

        }

        if (newResult instanceof InteractionResult.Success) {
            player.swing(hand);
        }

        return ActionResultUtil.shouldOverride(original, newResult)
            ? newResult
            : original;

    }

    @WrapOperation(method = "useItemOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;onUseWithItem(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/phys/BlockHitResult;)Lnet/minecraft/world/InteractionResult;"))
    private InteractionResult apoli$beforeItemUseOnBlock(BlockState state, ItemStack stack, Level world, Player player, InteractionHand hand, BlockHitResult hitResult, Operation<InteractionResult> original, @Share("zeroPriority$itemUseOnBlock") LocalRef<InteractionResult> zeroPriority$itemUseOnBlockRef) {

        ItemStack stackInHand = player.getItemInHand(hand);
        BlockUsagePhase usePhase = BlockUsagePhase.ITEM;

        if (PreventBlockUsePowerType.doesPrevent(player, usePhase, hitResult, stackInHand, hand)) {
            return InteractionResult.FAIL;
        }

        Prioritized.CallInstance<ActiveInteractionPowerType> aipci = new Prioritized.CallInstance<>();
        aipci.add(player, ActionOnBlockUsePowerType.class, p -> p.shouldExecute(usePhase, PriorityPhase.BEFORE, hitResult, hand, stackInHand));

        for (int i = aipci.getMaxPriority(); i >= aipci.getMinPriority(); i--) {

            if (!aipci.hasPowerTypes(i)) {
                continue;
            }

            List<ActiveInteractionPowerType> aips = aipci.getPowerTypes(i);
            InteractionResult previousResult = InteractionResult.PASS;

            for (ActiveInteractionPowerType aip : aips) {

                InteractionResult currentResult = aip instanceof ActionOnBlockUsePowerType aobup
                    ? aobup.executeAction(hitResult, hand)
                    : InteractionResult.PASS;

                if (ActionResultUtil.shouldOverride(previousResult, currentResult)) {
                    previousResult = currentResult;
                }

            }

            if (i == 0) {
                zeroPriority$itemUseOnBlockRef.set(previousResult);
                continue;
            }

            if (previousResult == InteractionResult.PASS) {
                continue;
            }

            if (previousResult instanceof InteractionResult.Success) {
                player.swing(hand);
            }

            // InteractionResult is now a sealed interface in 26.1; return the result directly
            return previousResult;

        }

        return original.call(state, stack, world, player, hand, hitResult);

    }

    @ModifyReturnValue(method = "useItemOn", at = @At(value = "RETURN", ordinal = 0), slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/world/InteractionResult;isAccepted()Z")))
    private InteractionResult apoli$afterItemUseOnBlock(InteractionResult original, LocalPlayer player, InteractionHand hand, BlockHitResult hitResult, @Share("zeroPriority$itemUseOnBlock") LocalRef<InteractionResult> zeroPriority$itemUseOnBlockRef) {

        InteractionResult zeroPriority$itemUseOnBlock = zeroPriority$itemUseOnBlockRef.get();
        InteractionResult newResult = InteractionResult.PASS;

        if (zeroPriority$itemUseOnBlock != null && zeroPriority$itemUseOnBlock != InteractionResult.PASS) {
            newResult = zeroPriority$itemUseOnBlock;
        }

        else if (original == InteractionResult.PASS) {

            Prioritized.CallInstance<ActiveInteractionPowerType> aipci = new Prioritized.CallInstance<>();
            aipci.add(player, ActionOnBlockUsePowerType.class, p -> p.shouldExecute(BlockUsagePhase.ITEM, PriorityPhase.AFTER, hitResult, hand, player.getItemInHand(hand)));

            for (int i = aipci.getMaxPriority(); i >= aipci.getMinPriority(); i--) {

                if (!aipci.hasPowerTypes(i)) {
                    continue;
                }

                List<ActiveInteractionPowerType> aips = aipci.getPowerTypes(i);
                InteractionResult previousResult = InteractionResult.PASS;

                for (ActiveInteractionPowerType aip : aips) {

                    InteractionResult currentResult = aip instanceof ActionOnBlockUsePowerType aobup
                        ? aobup.executeAction(hitResult, hand)
                        : InteractionResult.PASS;

                    if (ActionResultUtil.shouldOverride(previousResult, currentResult)) {
                        previousResult = currentResult;
                    }

                }

                if (previousResult != InteractionResult.PASS) {
                    newResult = previousResult;
                    break;
                }

            }

        }

        if (newResult instanceof InteractionResult.Success) {
            player.swing(hand);
        }

        return ActionResultUtil.shouldOverride(original, newResult)
            ? newResult
            : original;

    }

}
