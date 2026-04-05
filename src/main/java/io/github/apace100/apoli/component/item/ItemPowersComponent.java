package io.github.apace100.apoli.component.item;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerManager;
import io.github.apace100.apoli.power.type.PowerType;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.Identifier;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class ItemPowersComponent {

    public static final ItemPowersComponent DEFAULT = new ItemPowersComponent(Set.of());

    public static final Codec<ItemPowersComponent> CODEC = Entry.SET_CODEC.xmap(
        ItemPowersComponent::new,
		ItemPowersComponent::entries
    );

    public static final StreamCodec<ByteBuf, ItemPowersComponent> PACKET_CODEC = ByteBufCodecs.collection(ObjectLinkedOpenHashSet::new, Entry.PACKET_CODEC).map(
        ItemPowersComponent::new,
        ItemPowersComponent::entries
    );

    final ObjectLinkedOpenHashSet<Entry> entries;

    ItemPowersComponent(Collection<Entry> entries) {
        this.entries = new ObjectLinkedOpenHashSet<>(entries);
    }

    @Override
    public String toString() {
        return "ItemPowersComponent{entries=" + entries + "}";
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }

        else if (!(obj instanceof ItemPowersComponent that)) {
            return false;
        }

        else {
            return Objects.equals(this.entries(), that.entries());
        }

    }

    @Override
    public int hashCode() {
        return Objects.hashCode(entries);
    }

    private ObjectLinkedOpenHashSet<Entry> entries() {
        return entries;
    }

    public Stream<Entry> stream() {
        return entries.stream();
    }

    public void appendTooltip(EquipmentSlotGroup modifierSlot, Item.TooltipContext context, Consumer<Component> tooltip, TooltipFlag type) {

        for (Entry entry : entries) {

            Power power = PowerManager.getNullable(entry.powerId());
            if (power == null || entry.hidden() || !entry.slot().equals(modifierSlot)) {
                continue;
            }

            tooltip.accept(Component
                .translatable("tooltip.apoli.stack_power.name", power.getName())
                .withStyle(entry.negative()
                    ? ChatFormatting.RED
                    : ChatFormatting.YELLOW));

            if (!type.isAdvanced()) {
                continue;
            }

            tooltip.accept(Component
                .translatable("tooltip.apoli.stack_power.description", power.getDescription())
                .withStyle(ChatFormatting.GRAY));

        }

    }

    public int matchingSlots(EquipmentSlotGroup modifierSlot) {
        return (int) entries
            .stream()
            .map(Entry::slot)
            .filter(modifierSlot::equals)
            .count();
    }

    public boolean containsSlot(EquipmentSlotGroup modifierSlot) {
        return entries
            .stream()
            .map(Entry::slot)
            .anyMatch(modifierSlot::equals);
    }

    public boolean isEmpty() {
        return entries.isEmpty();
    }

    public int size() {
        return entries.size();
    }

    /**
     * NBT key used to store per-power runtime state inside the item's CUSTOM_DATA.
     * Format: {"apoli_stack_power_data": {"power:id": <power_nbt_tag>, ...}}
     */
    private static final String STACK_POWER_DATA_KEY = "apoli_stack_power_data";

    /**
     * Saves the runtime state of all item-sourced powers from the entity into the stack's CUSTOM_DATA.
     * Called before revoking stack powers so the data is preserved when the item is dropped.
     */
    private static void saveStackPowerState(LivingEntity entity, Identifier sourceId, ItemStack stack, List<Power> powers) {

        PowerHolderComponent component = PowerHolderComponent.KEY.getNullable(entity);
        if (component == null || powers.isEmpty()) {
            return;
        }

        CompoundTag customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        CompoundTag powerDataTag = customData.getCompound(STACK_POWER_DATA_KEY)
            .orElseGet(CompoundTag::new);

        for (Power power : powers) {
            PowerType powerType = component.getPowerType(power);
            if (powerType != null) {
                Tag stateTag = powerType.toTag();
                if (stateTag != null) {
                    powerDataTag.put(power.getId().toString(), stateTag);
                }
            }
        }

        if (!powerDataTag.isEmpty()) {
            customData.put(STACK_POWER_DATA_KEY, powerDataTag);
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(customData));
        }

    }

    /**
     * Restores previously-saved runtime state of all item-sourced powers back into the entity.
     * Called after granting stack powers so that resource values, cooldowns, etc. are recovered.
     */
    private static void loadStackPowerState(LivingEntity entity, Identifier sourceId, ItemStack stack, List<Power> powers) {

        PowerHolderComponent component = PowerHolderComponent.KEY.getNullable(entity);
        if (component == null || powers.isEmpty()) {
            return;
        }

        if (!stack.has(DataComponents.CUSTOM_DATA)) {
            return;
        }

        CompoundTag customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (!customData.contains(STACK_POWER_DATA_KEY)) {
            return;
        }

        CompoundTag powerDataTag = customData.getCompound(STACK_POWER_DATA_KEY).orElseGet(CompoundTag::new);

        for (Power power : powers) {
            String key = power.getId().toString();
            if (powerDataTag.contains(key)) {
                PowerType powerType = component.getPowerType(power);
                if (powerType != null) {
                    powerType.fromTag(powerDataTag.get(key));
                }
            }
        }

    }

    public static void onChangeEquipment(LivingEntity entity, EquipmentSlot equipmentSlot, ItemStack previousStack, ItemStack currentStack) {

        Identifier sourceId = Apoli.identifier("item/" + equipmentSlot.getName());
        if (ItemStack.matches(previousStack, currentStack) || !PowerHolderComponent.KEY.isProvidedBy(entity)) {
            return;
        }

        List<Power> revokedPowers = new ObjectArrayList<>();
        ItemPowersComponent prevStackPowers = previousStack.getOrDefault(ApoliDataComponentTypes.POWERS, DEFAULT);

        for (Entry prevEntry : prevStackPowers.entries) {
            PowerManager.getOptional(prevEntry.powerId())
                .filter(power -> prevEntry.slot().test(equipmentSlot))
                .ifPresent(revokedPowers::add);
        }

        List<Power> grantedPowers = new ObjectArrayList<>();
        ItemPowersComponent currStackPowers = currentStack.getOrDefault(ApoliDataComponentTypes.POWERS, DEFAULT);

        for (Entry currEntry : currStackPowers.entries) {
            PowerManager.getOptional(currEntry.powerId())
                .filter(power -> currEntry.slot().test(equipmentSlot))
                .ifPresent(grantedPowers::add);
        }

        if (!revokedPowers.isEmpty()) {
            //  Save power state into the item stack before revoking so data survives drop/pickup
            saveStackPowerState(entity, sourceId, previousStack, revokedPowers);
            PowerHolderComponent.revokePowers(entity, Map.of(sourceId, revokedPowers), true);
        }

        if (!grantedPowers.isEmpty()) {
            PowerHolderComponent.grantPowers(entity, Map.of(sourceId, grantedPowers), true);
            //  Restore previous power state from the item stack after granting
            loadStackPowerState(entity, sourceId, currentStack, grantedPowers);
        }

    }

    public record Entry(Identifier powerId, EquipmentSlotGroup slot, boolean hidden, boolean negative) {

        public static final MapCodec<Entry> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Identifier.CODEC.fieldOf("power").forGetter(Entry::powerId),
            EquipmentSlotGroup.CODEC.fieldOf("slot").forGetter(Entry::slot),
            Codec.BOOL.optionalFieldOf("hidden", false).forGetter(Entry::hidden),
            Codec.BOOL.optionalFieldOf("negative", false).forGetter(Entry::negative)
        ).apply(instance, Entry::new));

        public static final StreamCodec<ByteBuf, Entry> PACKET_CODEC = StreamCodec.composite(
            Identifier.STREAM_CODEC, Entry::powerId,
            EquipmentSlotGroup.STREAM_CODEC, Entry::slot,
            ByteBufCodecs.BOOL, Entry::hidden,
            ByteBufCodecs.BOOL, Entry::negative,
            Entry::new
        );

        public static final Codec<Set<Entry>> SET_CODEC = MAP_CODEC.codec().listOf().xmap(
			ImmutableSet::copyOf,
			ImmutableList::copyOf
        );

        @Override
        public boolean equals(Object obj) {

            if (this == obj) {
                return true;
            }

            else if (obj instanceof Entry other) {
                return this.powerId().equals(other.powerId())
                    && this.slot().equals(other.slot());
            }

            else {
                return false;
            }

        }

        @Override
        public int hashCode() {
            return Objects.hash(powerId, slot);
        }

    }

    public static Builder builder() {
        return builder(DEFAULT);
    }

    public static Builder builder(ItemPowersComponent baseItemPowers) {
        return new Builder(baseItemPowers);
    }

    public static class Builder {

        private final ObjectLinkedOpenHashSet<Entry> entries = new ObjectLinkedOpenHashSet<>();

        private Builder(ItemPowersComponent baseItemPowers) {
            this.entries.addAll(baseItemPowers.entries);
        }

        public Builder add(EnumSet<EquipmentSlotGroup> slots, Identifier powerId, boolean hidden, boolean negative) {

            CompoundTag entryNbt = new CompoundTag();
            for (EquipmentSlotGroup slot : slots) {

                entryNbt.putString("slot", slot.getSerializedName());
                entryNbt.putString("power", powerId.toString());
                entryNbt.putBoolean("hidden", hidden);
                entryNbt.putBoolean("negative", negative);

                Entry.MAP_CODEC.codec().parse(NbtOps.INSTANCE, entryNbt)
                    .resultOrPartial(err -> Apoli.LOGGER.warn("Cannot add element ({}) as an item power entry: {}", entryNbt, err))
                    .ifPresent(entries::add);

            }

            return this;

        }

        public Builder remove(EnumSet<EquipmentSlotGroup> slots, Identifier powerId) {
            return remove(slots, powerId, modifierSlot -> {});
        }

        public Builder remove(EnumSet<EquipmentSlotGroup> slots, Identifier powerId, Consumer<Collection<Entry>> removalCallback) {

            ObjectListIterator<Entry> entryIterator = entries.iterator();
            ObjectLinkedOpenHashSet<Entry> removedEntries = new ObjectLinkedOpenHashSet<>();

            while (entryIterator.hasNext()) {

                Entry entry = entryIterator.next();

                if (entry.powerId().equals(powerId) && slots.contains(entry.slot())) {
                    removedEntries.add(entry);
                    entryIterator.remove();
                }

            }

            if (!removedEntries.isEmpty()) {
                removalCallback.accept(removedEntries);
            }

            return this;

        }

        public Builder remove(Predicate<Entry> entryPredicate) {
            entries.removeIf(entryPredicate);
            return this;
        }

        public ItemPowersComponent build() {
            return !entries.isEmpty()
                ? new ItemPowersComponent(entries)
                : DEFAULT;
        }

    }

}
