package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.access.EntityLinkedItemStack;
import io.github.apace100.apoli.action.EntityAction;
import io.github.apace100.apoli.action.ItemAction;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.condition.ItemCondition;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.power.PowerConfiguration;
import io.github.apace100.apoli.util.InventoryUtil;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.Optional;

public class EdibleItemPowerType extends PowerType implements Prioritized<EdibleItemPowerType> {

    public static final TypedDataObjectFactory<EdibleItemPowerType> DATA_FACTORY = PowerType.createConditionedDataFactory(
        new SerializableData()
            .add("entity_action", EntityAction.DATA_TYPE.optional(), Optional.empty())
            .add("item_action", ItemAction.DATA_TYPE.optional(), Optional.empty())
            .add("result_item_action", ItemAction.DATA_TYPE.optional(), Optional.empty())
            .add("item_condition", ItemCondition.DATA_TYPE.optional(), Optional.empty())
            .add("food_component", SerializableDataTypes.FOOD_COMPONENT)
            .add("result_stack", SerializableDataTypes.ITEM_STACK.optional(), Optional.empty())
            .add("consume_animation", SerializableDataType.enumValue(ItemUseAnimation.class), ItemUseAnimation.EAT)
            .add("consume_sound", SerializableDataTypes.SOUND_EVENT, SoundEvents.ENTITY_GENERIC_EAT)
            .add("priority", SerializableDataTypes.INT, 0),
        (data, condition) -> new EdibleItemPowerType(
            data.get("entity_action"),
            data.get("item_action"),
            data.get("result_item_action"),
            data.get("item_condition"),
            data.get("food_component"),
            data.get("result_stack"),
            data.get("consume_animation"),
            data.get("consume_sound"),
            data.get("priority"),
            condition
        ),
        (powerType, serializableData) -> serializableData.instance()
            .set("entity_action", powerType.entityAction)
            .set("item_action", powerType.consumedItemAction)
            .set("result_item_action", powerType.resultItemAction)
            .set("item_condition", powerType.itemCondition)
            .set("food_component", powerType.foodComponent)
            .set("result_stack", powerType.resultStack)
            .set("consume_animation", powerType.consumeAnimation)
            .set("consume_sound", powerType.consumeSoundEvent)
            .set("priority", powerType.getPriority())
    );

    private final Optional<EntityAction> entityAction;
    private final Optional<ItemAction> resultItemAction;
    private final Optional<ItemAction> consumedItemAction;

    private final Optional<ItemCondition> itemCondition;

    private final FoodProperties foodComponent;
    private final Optional<ItemStack> resultStack;
    private final ItemUseAnimation consumeAnimation;
    private final SoundEvent consumeSoundEvent;

    private final int priority;

    public EdibleItemPowerType(Optional<EntityAction> entityAction, Optional<ItemAction> consumedItemAction, Optional<ItemAction> resultItemAction, Optional<ItemCondition> itemCondition, FoodProperties foodComponent, Optional<ItemStack> resultStack, ItemUseAnimation consumeAnimation, SoundEvent consumeSoundEvent, int priority, Optional<EntityCondition> condition) {
        super(condition);
        this.entityAction = entityAction;
        this.consumedItemAction = consumedItemAction;
        this.resultItemAction = resultItemAction;
        this.itemCondition = itemCondition;
        this.foodComponent = foodComponent;
        this.resultStack = resultStack;
        this.consumeAnimation = consumeAnimation;
        this.consumeSoundEvent = consumeSoundEvent;
        this.priority = priority;
    }

    @Override
    public @NotNull PowerConfiguration<?> getConfig() {
        return PowerTypes.EDIBLE_ITEM;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    public boolean doesApply(ItemStack stack) {
        return itemCondition
            .map(condition -> condition.test(getHolder().level(), stack))
            .orElse(true);
    }

    public void executeEntityAction() {
        entityAction.ifPresent(action -> action.execute(getHolder()));
    }

    public SlotAccess executeItemActions(SlotAccess consumedStackReference) {

        LivingEntity holder = getHolder();
        Level world = holder.level();

        consumedItemAction.ifPresent(action -> action.execute(world, consumedStackReference));

        SlotAccess resultStackReference = this.resultStack
            .map(ItemStack::copy)
            .map(InventoryUtil::createStackReference)
            .orElse(SlotAccess.EMPTY);

        resultItemAction.ifPresent(action -> action.execute(world, resultStackReference));
        return resultStackReference;

    }

    public FoodProperties getFoodComponent() {
        return foodComponent;
    }

    public ItemUseAnimation getConsumeAnimation() {
        return consumeAnimation;
    }

    public SoundEvent getConsumeSoundEvent() {
        return consumeSoundEvent;
    }

    public static Optional<EdibleItemPowerType> get(ItemStack stack, @Nullable Entity holder) {
        return PowerHolderComponent.getPowerTypes(holder, EdibleItemPowerType.class)
            .stream()
            .filter(p -> p.doesApply(stack))
            .max(Comparator.comparing(EdibleItemPowerType::getPriority))
            .filter(p -> !stack.contains(DataComponents.FOOD) || p.getPriority() > 1);
    }

    public static Optional<EdibleItemPowerType> get(ItemStack stack) {
        Entity stackHolder = ((EntityLinkedItemStack) stack).apoli$getEntity(true);
        return get(stack, stackHolder);
    }

}
