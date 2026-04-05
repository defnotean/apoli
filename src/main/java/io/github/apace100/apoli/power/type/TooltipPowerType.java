package io.github.apace100.apoli.power.type;

import com.google.common.collect.Lists;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.DataResult;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.condition.ItemCondition;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.power.PowerConfiguration;
import io.github.apace100.apoli.util.MiscUtil;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.RegistryOps;
import net.minecraft.core.HolderLookup;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.ComponentUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.function.Consumer;

public class TooltipPowerType extends PowerType {

    public static final TypedDataObjectFactory<TooltipPowerType> DATA_FACTORY = PowerType.createConditionedDataFactory(
        new SerializableData()
            .add("item_condition", ItemCondition.DATA_TYPE.optional(), Optional.empty())
            .add("text", SerializableDataTypes.TEXT, null)
            .addFunctionedDefault("texts", SerializableDataTypes.TEXTS, data -> MiscUtil.singletonListOrNull(data.get("text")))
            .add("should_resolve", SerializableDataTypes.BOOLEAN, false)
            .addFunctionedDefault("resolve", SerializableDataTypes.BOOLEAN, data -> data.get("should_resolve"))
            .add("tick_rate", SerializableDataTypes.INT, 20)
            .add("order", SerializableDataTypes.INT, 0)
            .validate(MiscUtil.validateAnyFieldsPresent("text", "texts")),
        (data, condition) -> new TooltipPowerType(
            data.get("item_condition"),
            data.get("texts"),
            data.get("resolve"),
            data.get("tick_rate"),
            data.get("order"),
            condition
        ),
        (powerType, serializableData) -> serializableData.instance()
            .set("item_condition", powerType.itemCondition)
            .set("texts", powerType.texts)
            .set("resolve", powerType.resolve)
            .set("tick_rate", powerType.tickRate)
            .set("order", powerType.order)
    );

    private final Optional<ItemCondition> itemCondition;
    private final List<Component> texts;

    private final boolean resolve;

    private final int tickRate;
    private final int order;

    private final ObjectArrayList<Component> tooltipTexts;

    private Integer startTicks;
    private Integer endTicks;

    private boolean wasActive;

    public TooltipPowerType(Optional<ItemCondition> itemCondition, List<Component> texts, boolean resolve, int tickRate, int order, Optional<EntityCondition> condition) {
        super(condition);

        this.itemCondition = itemCondition;
        this.texts = texts;

        this.resolve = resolve;

        this.tickRate = tickRate;
        this.order = order;

        this.tooltipTexts = new ObjectArrayList<>();

        this.startTicks = null;
        this.endTicks = null;

        this.wasActive = false;

    }

    @Override
    public @NotNull PowerConfiguration<?> getConfig() {
        return PowerTypes.TOOLTIP;
    }

    @Override
    public boolean shouldTick() {
        return resolve;
    }

    @Override
    public boolean shouldTickWhenInactive() {
        return shouldTick();
    }

    @Override
    public void serverTick() {

        LivingEntity holder = getHolder();
        int modTicks = holder.tickCount % tickRate;

        if (isActive()) {

            if (startTicks == null) {
                this.startTicks = modTicks;
                this.endTicks = null;
            }

            else if (modTicks == startTicks) {

                List<Component> parsedTexts = parseTexts();
                this.wasActive = true;

                if (!parsedTexts.isEmpty() && Collections.disjoint(tooltipTexts, parsedTexts)) {

                    this.tooltipTexts.clear();
                    this.tooltipTexts.addAll(parsedTexts);

                    this.tooltipTexts.trim();
                    PowerHolderComponent.syncPower(getHolder(), getPower());

                }

            }

        }

        else if (wasActive) {

            if (endTicks == null) {
                this.startTicks = null;
                this.endTicks = modTicks;
            }

            else if (modTicks == endTicks) {
                this.wasActive = false;
            }

        }

    }

    @Override
    public Tag toTag() {

        HolderLookup.Provider registryLookup = getHolder().registryAccess();
        RegistryOps<Tag> nbtOps = registryLookup.getOps(NbtOps.INSTANCE);

        CompoundTag rootNbt = new CompoundTag();
        ListTag tooltipTextsNbt = new ListTag();

        tooltipTexts.stream()
            .map(text -> ComponentSerialization.CODEC.encodeStart(nbtOps, text))
            .filter(DataResult::isSuccess)
            .map(DataResult::getOrThrow)
            .forEach(tooltipTextsNbt::add);

        rootNbt.put("Tooltips", tooltipTextsNbt);
        return rootNbt;

    }

    @Override
    public void fromTag(Tag tag) {

        HolderLookup.Provider registryLookup = getHolder().registryAccess();
        RegistryOps<Tag> nbtOps = registryLookup.getOps(NbtOps.INSTANCE);

        this.tooltipTexts.clear();

        CompoundTag rootNbt = (CompoundTag) tag;
        Tag tooltipTextsNbt = rootNbt.get("Tooltips");

        if (tooltipTextsNbt instanceof ListTag actualTooltipTextNbt) {

            actualTooltipTextNbt
                .stream()
                .map(nbtElement -> ComponentSerialization.CODEC.parse(nbtOps, nbtElement))
                .filter(DataResult::isSuccess)
                .map(DataResult::getOrThrow)
                .forEach(this.tooltipTexts::add);

        }

        this.tooltipTexts.trim();

    }

    public int getOrder() {
        return order;
    }

    public void processTooltips(Consumer<Component> processor) {

        if (resolve) {
            tooltipTexts.forEach(processor);
        }

        else {
            texts.forEach(processor);
        }

    }

    public boolean doesApply(ItemStack stack) {
        return itemCondition
            .map(condition -> condition.test(getHolder().level(), stack))
            .orElse(true);
    }

    private List<Component> parseTexts() {

        List<Component> parsedTexts = Lists.newLinkedList();
        LivingEntity holder = getHolder();

        if (texts.isEmpty() || !(holder.level() instanceof ServerLevel serverWorld)) {
            return parsedTexts;
        }

        ListIterator<Component> textIterator = texts.listIterator();
        CommandSourceStack source = holder.getCommandSource()
            .withOutput(serverWorld.getServer())
            .withLevel(Apoli.config.executeCommand.permissionLevel);

        while (textIterator.hasNext()) {

            Component text = textIterator.next();
            int index = textIterator.nextIndex();

            try {
                parsedTexts.add(ComponentUtils.parse(source, text, holder, 0));
            }

            catch (CommandSyntaxException cse) {
                Apoli.LOGGER.warn("Power {} couldn't parse tooltip text at index {}: {}", this.getPower().getId(), index, cse.getMessage());
            }

        }

        return parsedTexts;

    }

}
