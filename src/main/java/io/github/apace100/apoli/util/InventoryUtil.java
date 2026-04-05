package io.github.apace100.apoli.util;

import io.github.apace100.apoli.action.EntityAction;
import io.github.apace100.apoli.action.ItemAction;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.condition.ItemCondition;
import io.github.apace100.apoli.mixin.SlotRangesAccessor;
import io.github.apace100.apoli.power.type.InventoryPowerType;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.SlotRange;
import net.minecraft.world.inventory.SlotRanges;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import java.util.stream.IntStream;

public class InventoryUtil {

    public enum InventoryType {
        INVENTORY,
        POWER
    }

    public enum ProcessMode implements ToIntFunction<ItemStack> {

        STACKS {

            @Override
            public int applyAsInt(ItemStack value) {
                return 1;
            }

        },

        ITEMS {

            @Override
            public int applyAsInt(ItemStack value) {
                return value.getCount();
            }

        }

    }

    public static int checkInventory(Entity entity, IntCollection slots, Optional<InventoryPowerType> inventoryPowerType, Optional<ItemCondition> itemCondition, ProcessMode processMode) {

        IntSet preppedSlots = prepSlots(slots, entity, inventoryPowerType);
        int matches = 0;

        for (int preppedSlot : preppedSlots) {

            SlotAccess stackReference = getStackReference(entity, inventoryPowerType, preppedSlot);
            ItemStack stack = stackReference.get();

            if (itemCondition.map(condition -> condition.test(entity.level(), stack)).orElse(true)) {
                matches += processMode.applyAsInt(stack);
            }

        }

        return matches;

    }

    public static void modifyInventory(Entity entity, IntCollection slots, Optional<InventoryPowerType> inventoryPowerType, Optional<EntityAction> entityAction, ItemAction itemAction, Optional<ItemCondition> itemCondition, Optional<Integer> limit, ProcessMode processMode) {

        IntSet preppedSlots = prepSlots(slots, entity, inventoryPowerType);
        AtomicInteger processedItems = new AtomicInteger();

        modifyingItemsLoop:
        for (int preppedSlot : preppedSlots) {

            SlotAccess stackReference = getStackReference(entity, inventoryPowerType, preppedSlot);
            ItemStack stack = stackReference.get();

            if (!itemCondition.map(condition -> condition.test(entity.level(), stack)).orElse(true)) {
                continue;
            }

            int amount = processMode.applyAsInt(stack);
            for (int i = 0; i < amount; i++) {

                entityAction.ifPresent(action -> action.execute(entity));
                itemAction.execute(entity.level(), stackReference);

                if (limit.map(value -> processedItems.incrementAndGet() >= value).orElse(false)) {
                    break modifyingItemsLoop;
                }

            }

        }

    }

    public static void replaceInventory(Entity entity, IntCollection slots, Optional<InventoryPowerType> inventoryPowerType, Optional<EntityAction> entityAction, Optional<ItemAction> itemAction, Optional<ItemCondition> itemCondition, ItemStack replacementStack, boolean mergeNbt) {

        IntSet preppedSlots = prepSlots(slots, entity, inventoryPowerType);
        for (int preppedSlot : preppedSlots) {

            SlotAccess stackReference = getStackReference(entity, inventoryPowerType, preppedSlot);
            ItemStack stack = stackReference.get();

            if (!itemCondition.map(condition -> condition.test(entity.level(), stack)).orElse(true)) {
                continue;
            }

            ItemStack replacementStackCopy = replacementStack.copy();
            entityAction.ifPresent(action -> action.execute(entity));

            if (mergeNbt) {
                //  TODO: Either keep this as is, or re-implement it to merge components in a possibly hacky way (I'd rather not)   -eggohito
                CompoundTag originalStackNbt = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
                CustomData.update(DataComponents.CUSTOM_DATA, replacementStackCopy, replacementStackNbt -> replacementStackNbt.merge(originalStackNbt));
            }

            stackReference.set(replacementStackCopy);
            itemAction.ifPresent(action -> action.execute(entity.level(), stackReference));

        }

    }

    public static void dropInventory(Entity entity, IntCollection slots, Optional<InventoryPowerType> inventoryPowerType, Optional<EntityAction> entityAction, Optional<ItemAction> itemAction, Optional<ItemCondition> itemCondition, boolean throwRandomly, boolean retainOwnership, Optional<Integer> amount) {

        IntSet preppedSlots = prepSlots(slots, entity, inventoryPowerType);
        for (int preppedSlot : preppedSlots) {

            SlotAccess stackReference = getStackReference(entity, inventoryPowerType, preppedSlot);
            ItemStack stack = stackReference.get();

            if (stack.isEmpty() || !itemCondition.map(condition -> condition.test(entity.level(), stack)).orElse(true)) {
                continue;
            }

            entityAction.ifPresent(action -> action.execute(entity));
            itemAction.ifPresent(action -> action.execute(entity.level(), stackReference));

            ItemStack droppedStack = amount
                .map(Math::abs)
                .map(stack::split)
                .orElse(ItemStack.EMPTY);

            throwItem(entity, droppedStack.isEmpty() ? stack : droppedStack, throwRandomly, retainOwnership);
            stackReference.set(droppedStack.isEmpty() ? ItemStack.EMPTY : stack);

        }

    }

    public static void throwItem(Entity thrower, ItemStack stack, boolean throwRandomly, boolean retainOwnership) {
        throwItem(thrower, stack, throwRandomly, retainOwnership, 40);
    }

    public static void throwItem(Entity thrower, ItemStack itemStack, boolean throwRandomly, boolean retainOwnership, int pickupDelay) {

        if (itemStack.isEmpty()) {
            return;
        }

        if (thrower instanceof Player playerEntity && playerEntity.level().isClientSide()) {
            playerEntity.swing(InteractionHand.MAIN_HAND);
        }

        double yOffset = thrower.getEyeY() - 0.30000001192092896D;

        ItemEntity itemEntity = new ItemEntity(thrower.level(), thrower.getX(), yOffset, thrower.getZ(), itemStack);
        itemEntity.setPickUpDelay(pickupDelay);

        RandomSource random = RandomSource.create();

        float f;
        float g;

        if (retainOwnership) {
            itemEntity.setThrower(thrower);
        }

        if (throwRandomly) {

            f = random.nextFloat() * 0.5F;
            g = random.nextFloat() * 6.2831855F;

            itemEntity.setDeltaMovement(- Mth.sin(g) * f, 0.20000000298023224D, Mth.cos(g) * f);

        }

        else {

            f = 0.3F;
            g = Mth.sin(thrower.getXRot() * 0.017453292F);

            float h = Mth.cos(thrower.getXRot() * 0.017453292F);
            float i = Mth.sin(thrower.getYRot() * 0.017453292F);
            float j = Mth.cos(thrower.getYRot() * 0.017453292F);

            float k = random.nextFloat() * 6.2831855F;
            float l = 0.02F * random.nextFloat();

            itemEntity.setDeltaMovement(
                (double) (- i * h * f) + Math.cos(k) * (double) l,
                (-g * f + 0.1F + (random.nextFloat() - random.nextFloat()) * 0.1F),
                (double) (j * h * f) + Math.sin(k) * (double) l
            );

        }

        thrower.level().addFreshEntity(itemEntity);

    }

    public static void forEachStack(Entity entity, Consumer<ItemStack> stackConsumer) {

        OptionalInt slotToSkip = getSelectedHotBarSlot(entity);
        for (int slot : getAllSlots()) {

            if (slotToSkip.isPresent() && slotToSkip.getAsInt() == slot) {
                continue;
            }

            SlotAccess stackReference = entity.getSlot(slot);
            ItemStack stack = stackReference.get();

            if (!stack.isEmpty()) {
                stackConsumer.accept(stack);
            }

        }

        List<InventoryPowerType> inventoryPowerTypes = PowerHolderComponent.getOptional(entity)
            .stream()
            .map(component -> component.getPowerTypes(InventoryPowerType.class))
            .flatMap(Collection::stream)
            .toList();

        for (InventoryPowerType inventoryPowerType : inventoryPowerTypes) {

            for (int i = 0; i < inventoryPowerTypes.size(); i++) {

                ItemStack stack = inventoryPowerType.getItem(i);

                if (!stack.isEmpty()) {
                    stackConsumer.accept(stack);
                }

            }

        }

    }

    public static SlotAccess getStackReferenceFromStack(Entity entity, ItemStack stack) {
        return getStackReferenceFromStack(entity, stack, (provStack, refStack) -> provStack == refStack);
    }

    public static SlotAccess getStackReferenceFromStack(Entity entity, ItemStack stack, BiPredicate<ItemStack, ItemStack> equalityPredicate) {

        OptionalInt slotToSkip = getSelectedHotBarSlot(entity);
        for (int slot : getAllSlots()) {

            if (slotToSkip.isPresent() && slotToSkip.getAsInt() == slot) {
                continue;
            }

            SlotAccess stackReference = entity.getSlot(slot);
            if (stackReference != null && equalityPredicate.test(stack, stackReference.get())) {
                return stackReference;
            }

        }

        return SlotAccess.of(() -> ItemStack.EMPTY, s -> {});

    }

    /**
     *      <p>For players, their selected hotbar slot will overlap with the `weapon.mainhand` slot reference. This
     *      method returns the slot ID of the selected hotbar slot.</p>
     *
     *      @param entity   The entity to get the slot ID of its selected hotbar slot
     *      @return         The slot ID of the hotbar slot or {@link Integer#MIN_VALUE} if the entity is not a player
     */
    private static OptionalInt getSelectedHotBarSlot(Entity entity) {

        SlotRange slotRange = entity instanceof Player player
            ? SlotRanges.nameToIds("hotbar." + player.getInventory().getSelectedSlot())
            : null;

        return slotRange != null
            ? OptionalInt.of(slotRange.slots().getFirst())
            : OptionalInt.empty();

    }

    /**
     *  <p>Checks whether the specified {@code slot} index is within the bounds of the specified {@link InventoryPowerType},
     *  or the entity's {@link SlotAccess}, in that order.</p>
     *
     *  @param entity               the entity to check the bounds of its {@link SlotAccess}
     *  @param inventoryPowerType   the {@link InventoryPowerType} to check the bounds of (if present)
     *  @param slot                 the slot index
     *  @return                     {@code true} if the slot index is within the bounds
     */
    public static boolean slotWithinBounds(Entity entity, Optional<InventoryPowerType> inventoryPowerType, int slot) {
        return inventoryPowerType
            .map(powerType -> slot >= 0 && slot < powerType.getContainerSize())
            .orElseGet(() -> !entity.getSlot(slot).get().isEmpty());
    }

    public static SlotAccess getStackReference(@NotNull Entity entity, Optional<InventoryPowerType> inventoryPowerType, int slot) {
        return inventoryPowerType
            .<SlotAccess>map(powerType -> SlotAccess.of(() -> powerType.getItem(slot), stack -> powerType.setItem(slot, stack)))
            .orElseGet(() -> entity.getSlot(slot));
    }

    /**
     *      <p>Creates a stack reference that is not linked to any entity for use with item actions.</p>
     *
     *      <p>Recommended for usage when either you don't have an entity for this operation, or you
     *      don't want to set the entity's SlotAccess.</p>
     *
     *      @param startingStack The ItemStack that this reference will start with.
     *      @return A {@linkplain SlotAccess} that contains an ItemStack.
     */
    public static SlotAccess createStackReference(ItemStack startingStack) {
        return new SlotAccess() {

            ItemStack stack = startingStack;

            @Override
            public ItemStack get() {
                return stack;
            }

            @Override
            public boolean set(ItemStack stack) {
                this.stack = stack;
                return true;
            }

        };
    }

    private static final IntSet ALL_SLOTS = new IntOpenHashSet();

    public static IntSet getAllSlots() {

        if (ALL_SLOTS.isEmpty()) {

            for (SlotRange slotRange : SlotRangesAccessor.getSlotRanges()) {
                ALL_SLOTS.addAll(slotRange.slots());
            }

        }

        return ALL_SLOTS;

    }

    public static IntSet prepSlots(IntCollection slots, Entity entity, Optional<InventoryPowerType> inventoryPowerType) {

        IntStream slotStream = slots.isEmpty()
            ? getAllSlots().intStream()
            : slots.intStream();

        return slotStream
            .filter(slot -> slotWithinBounds(entity, inventoryPowerType, slot))
            .collect(IntOpenHashSet::new, IntOpenHashSet::add, IntOpenHashSet::addAll);

    }

    public static OptionalInt getSlotFromStackReference(Entity entity, SlotAccess stackReference) {

        for (int slot : getAllSlots()) {

            SlotAccess queriedStackRef = entity.getSlot(slot);

            if (queriedStackRef != null && queriedStackRef.equals(stackReference)) {
                return OptionalInt.of(slot);
            }

        }

        return OptionalInt.empty();

    }

}
