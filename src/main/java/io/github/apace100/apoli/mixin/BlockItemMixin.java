package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.type.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(BlockItem.class)
public class BlockItemMixin {

    @ModifyReturnValue(method = "canPlace", at = @At("RETURN"))
    private boolean apoli$preventBlockPlace(boolean original, BlockPlaceContext context, BlockState state) {

        Player playerEntity = context.getPlayer();
        if (playerEntity == null) {
            return original;
        }

        Direction direction = context.getDirection();
        ItemStack stack = context.getStack();
        InteractionHand hand = context.getHand();

        BlockPos toPos = context.blockPosition();
        BlockPos onPos = ((ItemUsageContextAccessor) context).callGetHitResult().blockPosition();

        Prioritized.CallInstance<ActiveInteractionPowerType> aipci = new Prioritized.CallInstance<>();
        int preventBlockPlacePowers = 0;

        aipci.add(playerEntity, PreventBlockPlacePowerType.class, pbpp -> pbpp.doesPrevent(stack, hand, toPos, onPos, direction));

        for (int i = aipci.getMaxPriority(); i >= aipci.getMinPriority(); i--) {

            if (!aipci.hasPowerTypes(i)) {
                continue;
            }

            List<PreventBlockPlacePowerType> pbpps = aipci.getPowerTypes(i)
                .stream()
                .filter(p -> p instanceof PreventBlockPlacePowerType)
                .map(p -> (PreventBlockPlacePowerType) p)
                .toList();

            preventBlockPlacePowers += pbpps.size();
            pbpps.forEach(pbpp -> pbpp.executeActions(hand, toPos, onPos, direction));

        }

        return preventBlockPlacePowers <= 0 && original;

    }

    @Inject(method = "place(Lnet/minecraft/world/item/context/BlockPlaceContext;)Lnet/minecraft/world/InteractionResult;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/Level;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"))
    private void apoli$actionOnBlockPlace(BlockPlaceContext context, CallbackInfoReturnable<InteractionResult> cir, @Local Player user, @Local BlockPos toPos, @Local ItemStack stack, @Share("aipci") LocalRef<Prioritized.CallInstance<ActiveInteractionPowerType>> aipciRef) {

        if (user == null) {
            return;
        }

        Direction direction = context.getDirection();
        BlockPos onPos = ((ItemUsageContextAccessor) context).callGetHitResult().blockPosition();
        InteractionHand hand = context.getHand();

        Prioritized.CallInstance<ActiveInteractionPowerType> aipci = new Prioritized.CallInstance<>();
        aipci.add(user, ActionOnBlockPlacePowerType.class, aobpp -> aobpp.shouldExecute(stack, hand, toPos, onPos, direction));

        for (int i = aipci.getMaxPriority(); i >= aipci.getMinPriority(); i--) {
            aipci.getPowerTypes(i)
                .stream()
                .filter(p -> p instanceof ActionOnBlockPlacePowerType)
                .forEach(p -> ((ActionOnBlockPlacePowerType) p).executeOtherActions(toPos, onPos, direction));
        }

        aipciRef.set(aipci);

    }

    @Inject(method = "place(Lnet/minecraft/world/item/context/BlockPlaceContext;)Lnet/minecraft/world/InteractionResult;", at = @At("TAIL"))
    private void apoli$actionOnBlockPlacePost(BlockPlaceContext context, CallbackInfoReturnable<InteractionResult> cir, @Share("aipci") LocalRef<Prioritized.CallInstance<ActiveInteractionPowerType>> aipciRef) {

        Prioritized.CallInstance<ActiveInteractionPowerType> aipci = aipciRef.get();

        if (aipci != null) {

            for (int i = aipci.getMaxPriority(); i >= aipci.getMinPriority(); i--) {
                aipci.getPowerTypes(i)
                    .stream()
                    .filter(ActionOnBlockPlacePowerType.class::isInstance)
                    .map(ActionOnBlockPlacePowerType.class::cast)
                    .forEach(p -> p.executeItemActions(context.getHand()));
            }

        }

    }

    @WrapOperation(method = "useOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/Item;use(Lnet/minecraft/world/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;"))
    private InteractionResult apoli$preventItemUseIfFoodBlockItem(BlockItem instance, Level world, Player user, InteractionHand hand, Operation<InteractionResult> original) {
        ItemStack handStack = user.getItemInHand(hand);
        return PowerHolderComponent.hasPowerType(user, PreventItemUsePowerType.class, p -> p.doesPrevent(handStack))
            ? InteractionResult.FAIL
            : original.call(instance, world, user, hand);
    }

}
