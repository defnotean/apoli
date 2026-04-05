package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import io.github.apace100.apoli.access.EntityLinkedItemStack;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.type.ActionOnItemUsePowerType;
import io.github.apace100.apoli.power.type.EdibleItemPowerType;
import io.github.apace100.apoli.power.type.ItemOnItemPowerType;
import io.github.apace100.apoli.power.type.ModifyEnchantmentLevelPowerType;
import io.github.apace100.apoli.power.type.ModifyFoodPowerType;
import io.github.apace100.apoli.power.type.PreventItemUsePowerType;
import io.github.apace100.apoli.util.InventoryUtil;
import io.github.apace100.apoli.util.PriorityPhase;
import io.github.apace100.apoli.util.StackClickPhase;
import net.fabricmc.fabric.api.item.v1.FabricItemStack;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
// TODO: MC 26.1 - ItemUsage removed, consumeHeldItem inlined below
import net.minecraft.world.inventory.Slot;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.lang.ref.WeakReference;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements DataComponentHolder, EntityLinkedItemStack, FabricItemStack {

    @Nullable
    @Shadow
    public abstract Entity getHolder();

    @Shadow
    public abstract Item getItem();

    @Shadow
    public abstract boolean isEmpty();

    @Shadow
    public abstract ItemStack copy();

    @Unique
    @Nullable
    private WeakReference<Entity> apoli$holdingEntity;

    @Override
    public Entity apoli$getEntity() {
        return apoli$getEntity(true);
    }

    @Override
    public Entity apoli$getEntity(boolean prioritiseVanillaHolder) {
        Entity vanillaHolder = getHolder();
        if (prioritiseVanillaHolder && vanillaHolder != null) {
            return vanillaHolder;
        }
        if (apoli$holdingEntity != null) {
            return apoli$holdingEntity.get();
        }
        return null;
    }

    @Override
    public void apoli$setEntity(Entity entity) {
        this.apoli$holdingEntity = new WeakReference<>(entity);
    }

    @ModifyReturnValue(method = "copy", at = @At("RETURN"))
    private ItemStack apoli$passHolderOnCopy(ItemStack original) {

        Entity holder = this.apoli$getEntity();
        if (holder != null) {
            if (original.isEmpty()) {
                original = ModifyEnchantmentLevelPowerType.getOrCreateWorkableEmptyStack(holder);
            } else {
                ((EntityLinkedItemStack) original).apoli$setEntity(holder);
            }
        }

        return original;

    }

    @WrapOperation(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/Item;use(Lnet/minecraft/world/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;"))
    private InteractionResult<ItemStack> apoli$onItemUse(Item item, Level world, Player user, InteractionHand hand, Operation<InteractionResult<ItemStack>> original) {

        //  region  Prevent item use
        ItemStack thisAsStack = (ItemStack) (Object) this;
        if (PowerHolderComponent.hasPowerType(user, PreventItemUsePowerType.class, piup -> piup.doesPrevent(thisAsStack))) {
            return InteractionResult.fail(thisAsStack);
        }
        //  endregion

        //  region  Action on item before use
        SlotAccess useStackReference = InventoryUtil.getStackReferenceFromStack(user, thisAsStack);
        ItemStack useStack = useStackReference.get();

        ActionOnItemUsePowerType.TriggerType triggerType = useStack.getMaxUseTime(user) == 0
            ? ActionOnItemUsePowerType.TriggerType.INSTANT
            : ActionOnItemUsePowerType.TriggerType.START;
        ActionOnItemUsePowerType.executeActions(user, useStackReference, useStack, triggerType, PriorityPhase.BEFORE);
        //  endregion

        //  region  Edible item
        ItemStack oldUseStack = useStack.copy();
        boolean canConsumeCustomFood = EdibleItemPowerType.get(useStack, user)
            .map(EdibleItemPowerType::getFoodComponent)
            .map(fc -> user.canConsume(fc.canAlwaysEat()))
            .orElse(false);

        InteractionResult<ItemStack> action;
        if (canConsumeCustomFood) {
            user.startUsingItem(hand);
            action = InteractionResult.CONSUME;
        } else {
            action = original.call(useStack.getItem(), world, user, hand);
        }

        if (!action.getResult().isAccepted()) {
            return action;
        }
        //  endregion

        //  region  Action on item after use
        useStackReference = SlotAccess.of(user, user.getPreferredEquipmentSlot(oldUseStack));
        triggerType = useStack.getMaxUseTime(user) == 0
            ? ActionOnItemUsePowerType.TriggerType.INSTANT
            : ActionOnItemUsePowerType.TriggerType.START;

        ActionOnItemUsePowerType.executeActions(user, useStackReference, useStack, triggerType, PriorityPhase.AFTER);
        return action;
        //  endregion

    }

    @WrapOperation(method = "onUseTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/Item;usageTick(Lnet/minecraft/world/Level;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;I)V"))
    private void apoli$actionOnItemDuringUse(Item item, Level world, LivingEntity user, ItemStack stack, int remainingUseTicks, Operation<Void> original, @Share("usingStackReference") LocalRef<SlotAccess> sharedUsingStackReference) {

        ActionOnItemUsePowerType.TriggerType triggerType = ActionOnItemUsePowerType.TriggerType.DURING;

        SlotAccess usingStackReference = InventoryUtil.getStackReferenceFromStack(user, (ItemStack) (Object) this);
        ItemStack usingStack = usingStackReference.get();

        ActionOnItemUsePowerType.executeActions(user, usingStackReference, usingStack, triggerType, PriorityPhase.BEFORE);

        if (EdibleItemPowerType.get(usingStack, user).isEmpty()) {
            original.call(usingStack.getItem(), world, user, usingStack, remainingUseTicks);
        }

        else {
            ActionOnItemUsePowerType.executeActions(user, usingStackReference, usingStack, triggerType, PriorityPhase.AFTER);
        }

    }

    @WrapOperation(method = "releaseUsing", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/Item;onStoppedUsing(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/Level;Lnet/minecraft/world/entity/LivingEntity;I)V"))
    private void apoli$actionOnItemStoppedUsing(Item item, ItemStack stack, Level world, LivingEntity user, int remainingUseTicks, Operation<Void> original, @Share("stoppedUsingStackReference") LocalRef<SlotAccess> sharedStoppedUsingStackReference) {

        ActionOnItemUsePowerType.TriggerType triggerType = ActionOnItemUsePowerType.TriggerType.STOP;

        SlotAccess stoppedUsingStackReference = InventoryUtil.getStackReferenceFromStack(user, (ItemStack) (Object) this);
        ItemStack stoppedUsingStack = stoppedUsingStackReference.get();

        ActionOnItemUsePowerType.executeActions(user, stoppedUsingStackReference, stoppedUsingStack, triggerType, PriorityPhase.BEFORE);

        if (EdibleItemPowerType.get(stoppedUsingStack, user).isEmpty()) {
            original.call(stoppedUsingStack.getItem(), stoppedUsingStack, world, user, remainingUseTicks);
        }

        else {
            ActionOnItemUsePowerType.executeActions(user, stoppedUsingStackReference, stoppedUsingStack, triggerType, PriorityPhase.AFTER);
        }

    }

    @WrapOperation(method = "finishUsingItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/Item;finishUsing(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/Level;Lnet/minecraft/world/entity/LivingEntity;)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack apoli$onFinishItemUse(Item item, ItemStack stack, Level world, LivingEntity user, Operation<ItemStack> original) {

        //  region  Action on item before finish using
        SlotAccess finishUsingStackRef = InventoryUtil.getStackReferenceFromStack(user, stack);
        ItemStack finishUsingStack = finishUsingStackRef.get();

        ActionOnItemUsePowerType.executeActions(user, finishUsingStackRef, finishUsingStack, ActionOnItemUsePowerType.TriggerType.FINISH, PriorityPhase.BEFORE);
        //  endregion

        //  region  Edible item consumption effects
        finishUsingStackRef.set(EdibleItemPowerType.get(finishUsingStack, user)
            .map(p -> user.eatFood(world, stack, p.getFoodComponent()))
            .orElseGet(() -> original.call(finishUsingStack.getItem(), finishUsingStack, world, user)));
        //  endregion

        //  region  Action on item after finish using
        ActionOnItemUsePowerType.executeActions(user, finishUsingStackRef, finishUsingStack, ActionOnItemUsePowerType.TriggerType.FINISH, PriorityPhase.AFTER);
        return finishUsingStack;
        //  endregion

    }

    @ModifyReturnValue(method = "getUseAnimation", at = @At("RETURN"))
    private ItemUseAnimation apoli$replaceUseAction(ItemUseAnimation original) {
        return EdibleItemPowerType.get((ItemStack) (Object) this)
            .map(EdibleItemPowerType::getConsumeAnimation)
            .orElse(original);
    }

    @ModifyReturnValue(method = "getEatingSound", at = @At("RETURN"))
    private SoundEvent apoli$replaceEatingSound(SoundEvent original) {
        return EdibleItemPowerType.get((ItemStack) (Object) this)
            .map(EdibleItemPowerType::getConsumeSoundEvent)
            .orElse(original);
    }

    @ModifyReturnValue(method = "getDrinkingSound", at = @At("RETURN"))
    private SoundEvent apoli$replaceDrinkingSound(SoundEvent original) {
        return EdibleItemPowerType.get((ItemStack) (Object) this)
            .map(EdibleItemPowerType::getConsumeSoundEvent)
            .orElse(original);
    }

    @ModifyReturnValue(method = "getUseDuration", at = @At("RETURN"))
    private int apoli$modifyMaxUseTicks(int original) {
        return ModifyFoodPowerType
            .modifyEatTicks(this.apoli$getEntity(), (ItemStack) (Object) this)
            .orElse(original);
    }

    @WrapOperation(method = "isUsedOnRelease", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/Item;isUsedOnRelease(Lnet/minecraft/world/item/ItemStack;)Z"))
    private boolean apoli$useOnReleaseIfCustomFood(Item item, ItemStack stack, Operation<Boolean> original) {
        return EdibleItemPowerType.get(stack).isEmpty()
            ? original.call(item, stack)
            : false;
    }

    @WrapOperation(method = "overrideStackedOnOther", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/Item;overrideStackedOnOther(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/inventory/Slot;Lnet/minecraft/world/inventory/ClickAction;Lnet/minecraft/world/entity/player/Player;)Z"))
    private boolean apoli$itemOnItem_cursorStack(Item cursorItem, ItemStack cursorStack, Slot slot, ClickAction clickType, Player player, Operation<Boolean> original) {

        StackClickPhase clickPhase = StackClickPhase.CURSOR;

        SlotAccess cursorStackReference = ((ScreenHandlerAccessor) player.currentScreenHandler).callGetCursorStackReference();
        SlotAccess slotStackReference = SlotAccess.of(slot.inventory, slot.getIndex());

        return ItemOnItemPowerType.executeActions(player, PriorityPhase.BEFORE, clickPhase, clickType, slot, slotStackReference, cursorStackReference)
            || original.call(cursorStackReference.get().getItem(), cursorStackReference.get(), slot, clickType, player)
            || ItemOnItemPowerType.executeActions(player, PriorityPhase.AFTER, clickPhase, clickType, slot, slotStackReference, cursorStackReference);

    }

    @WrapOperation(method = "overrideOtherStackedOnMe", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/Item;overrideOtherStackedOnMe(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/inventory/Slot;Lnet/minecraft/world/inventory/ClickAction;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/entity/SlotAccess;)Z"))
    private boolean apoli$itemOnItem_slotStack(Item slotItem, ItemStack slotStack, ItemStack cursorStack, Slot slot, ClickAction clickType, Player player, SlotAccess cursorStackReference, Operation<Boolean> original) {

        StackClickPhase clickPhase = StackClickPhase.SLOT;
        SlotAccess slotStackReference = SlotAccess.of(slot.inventory, slot.getIndex());

        return ItemOnItemPowerType.executeActions(player, PriorityPhase.BEFORE, clickPhase, clickType, slot, slotStackReference, cursorStackReference)
            || original.call(slotStackReference.get().getItem(), slotStackReference.get(), cursorStackReference.get(), slot, clickType, player, cursorStackReference)
            || ItemOnItemPowerType.executeActions(player, PriorityPhase.AFTER, clickPhase, clickType, slot, slotStackReference, cursorStackReference);

    }

}
