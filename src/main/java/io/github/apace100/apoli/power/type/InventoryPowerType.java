package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.condition.ItemCondition;
import io.github.apace100.apoli.data.ApoliContainerTypes;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.data.container.ContainerType;
import io.github.apace100.apoli.power.PowerConfiguration;
import io.github.apace100.apoli.util.keybinding.KeyBindingReference;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.TagValueInput;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

@SuppressWarnings("unused")
public class InventoryPowerType extends PowerType implements Active, Container {

    public static final TypedDataObjectFactory<InventoryPowerType> DATA_FACTORY = PowerType.createConditionedDataFactory(
        new SerializableData()
            .add("title", ApoliDataTypes.DEFAULT_TRANSLATABLE_TEXT, Component.translatable("container.inventory"))
            .add("container_type", ApoliDataTypes.CONTAINER_TYPE, ApoliContainerTypes.GENERIC_3X3)
            .add("drop_on_death_filter", ItemCondition.DATA_TYPE.optional(), Optional.empty())
            .add("key", ApoliDataTypes.BACKWARDS_COMPATIBLE_KEY, KeyBindingReference.NONE)
            .add("drop_on_death", SerializableDataTypes.BOOLEAN, false)
            .add("recoverable", SerializableDataTypes.BOOLEAN, true),
        (data, condition) -> new InventoryPowerType(
            data.get("title"),
            data.get("container_type"),
            data.get("drop_on_death_filter"),
            data.get("key"),
            data.get("drop_on_death"),
            data.get("recoverable"),
            condition
        ),
        (powerType, serializableData) -> serializableData.instance()
            .set("title", powerType.containerTitle)
            .set("container_type", powerType.containerType)
            .set("drop_on_death_filter", powerType.dropOnDeathFilter)
            .set("key", powerType.getKey())
            .set("drop_on_death", powerType.shouldDropOnDeath)
            .set("recoverable", powerType.recoverable)
    );

    private final Component containerTitle;
    private final ContainerType containerType;

    private final Optional<ItemCondition> dropOnDeathFilter;
    private final KeyBindingReference key;

    private final boolean shouldDropOnDeath;
    private final boolean recoverable;

    private final NonNullList<ItemStack> container;
    private final MenuProvider containerHandlerFactory;

    private boolean dirty;

    public InventoryPowerType(Component containerTitle, ContainerType containerType, Optional<ItemCondition> dropOnDeathFilter, KeyBindingReference key, boolean shouldDropOnDeath, boolean recoverable, Optional<EntityCondition> condition) {
        super(condition);
        this.containerTitle = containerTitle;
        this.containerType = containerType;
        this.dropOnDeathFilter = dropOnDeathFilter;
        this.key = key;
        this.shouldDropOnDeath = shouldDropOnDeath;
        this.recoverable = recoverable;
        this.container = NonNullList.withSize(containerType.size(), ItemStack.EMPTY);
        this.containerHandlerFactory = containerType.create(this);
        this.setTicking(true);
    }

    @Override
    public @NotNull PowerConfiguration<?> getConfig() {
        return PowerTypes.INVENTORY;
    }

    @Override
    public void onLost() {

        if (recoverable) {
            dropItemsOnLost();
        }

    }

    @Override
    public void onUse() {

        if (this.isActive() && getHolder() instanceof Player player) {
            player.openMenu(new SimpleMenuProvider(containerHandlerFactory, containerTitle));
        }

    }

    @Override
    public void serverTick() {

        if (dirty) {
            PowerHolderComponent.syncPower(getHolder(), getPower());
        }

        this.dirty = false;

    }

    @Override
    public CompoundTag toTag() {

        TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, getHolder().registryAccess());
        ContainerHelper.saveAllItems(output, container);

        return output.buildResult();

    }

    @Override
    public void fromTag(Tag tag) {

        if (!(tag instanceof CompoundTag rootNbt)) {
            return;
        }

        this.clearContent();
        ContainerHelper.loadAllItems(TagValueInput.create(ProblemReporter.DISCARDING, getHolder().registryAccess(), rootNbt), container);

    }

    @Override
    public int getContainerSize() {
        return container.size();
    }

    @Override
    public boolean isEmpty() {
        return container.isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        return container.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {

        ItemStack stack = ContainerHelper.removeItem(container, slot, amount);
        if (!stack.isEmpty()) {
            this.setChanged();
        }

        return stack;

    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {

        ItemStack prevStack = this.getItem(slot);
        this.setItem(slot, ItemStack.EMPTY);

        return prevStack;

    }

    @Override
    public void setItem(int slot, ItemStack stack) {

        container.set(slot, stack);
        if (!stack.isEmpty()) {
            stack.setCount(Math.min(stack.getCount(), this.getMaxStackSize()));
        }

        this.setChanged();

    }

    @Override
    public void setChanged() {
        this.dirty = true;
    }

    @Override
    public boolean stillValid(Player player) {
        return player == getHolder();
    }

    @Override
    public void clearContent() {
        this.container.clear();
        this.setChanged();
    }

    @Override
    public KeyBindingReference getKey() {
        return key;
    }

    public NonNullList<ItemStack> getContainer() {
        return container;
    }

    public Component getContainerTitle() {
        return containerTitle;
    }

    public MenuProvider getContainerHandlerFactory() {
        return containerHandlerFactory;
    }

    public boolean shouldDropOnDeath() {
        return shouldDropOnDeath;
    }

    public boolean shouldDropOnDeath(ItemStack stack) {
        return shouldDropOnDeath()
            && dropOnDeathFilter.map(condition -> condition.test(getHolder().level(), stack)).orElse(true);
    }

    public void dropItemsOnDeath() {

        if (!(getHolder() instanceof Player playerEntity) || playerEntity.level().isClientSide()) {
            return;
        }

        for (int i = 0; i < container.size(); ++i) {

            ItemStack currentStack = this.getItem(i).copy();
            if (!this.shouldDropOnDeath(currentStack)) {
                continue;
            }

            this.removeItemNoUpdate(i);
            if (!EnchantmentHelper.has(currentStack, EnchantmentEffectComponents.PREVENT_EQUIPMENT_DROP)) {
                playerEntity.drop(currentStack, true);
            }

        }

    }

    public void dropItemsOnLost() {

        if (!(getHolder() instanceof Player playerEntity) || playerEntity.level().isClientSide()) {
            return;
        }

        for (int i = 0; i < container.size(); ++i) {
            playerEntity.getInventory().add(this.getItem(i));
        }

    }

}
