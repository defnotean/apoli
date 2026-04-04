package io.github.apace100.apoli.loot.function;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.apace100.apoli.component.item.ApoliDataComponentTypes;
import io.github.apace100.apoli.component.item.ItemPowersComponent;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.PowerReference;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;

import java.util.EnumSet;
import java.util.List;

public class AddPowerLootFunction extends LootItemConditionalFunction {

    public static final MapCodec<AddPowerLootFunction> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> addConditionsField(instance).and(instance.group(
        SerializableDataTypes.ATTRIBUTE_MODIFIER_SLOT_SET.codec().optionalFieldOf("slot", EnumSet.of(EquipmentSlotGroup.ANY)).forGetter(AddPowerLootFunction::slots),
        ApoliDataTypes.POWER_REFERENCE.codec().fieldOf("power").forGetter(AddPowerLootFunction::power),
        Codec.BOOL.optionalFieldOf("hidden", false).forGetter(AddPowerLootFunction::hidden),
        Codec.BOOL.optionalFieldOf("negative", false).forGetter(AddPowerLootFunction::negative)
    )).apply(instance, AddPowerLootFunction::new));

    private final EnumSet<EquipmentSlotGroup> slots;
    private final PowerReference power;

    private final boolean hidden;
    private final boolean negative;

    private AddPowerLootFunction(List<LootItemCondition> conditions, EnumSet<EquipmentSlotGroup> slots, PowerReference power, boolean hidden, boolean negative) {
        super(conditions);
        this.slots = slots;
        this.power = power;
        this.hidden = hidden;
        this.negative = negative;
    }

    @Override
    public LootItemFunctionType<? extends LootItemConditionalFunction> getType() {
        return ApoliLootFunctionTypes.ADD_POWER;
    }

    @Override
    public ItemStack process(ItemStack stack, LootContext context) {

        power().getOptionalPower().ifPresent(power -> {

            ItemPowersComponent itemPowers = stack.getOrDefault(ApoliDataComponentTypes.POWERS, ItemPowersComponent.DEFAULT);
            stack.set(ApoliDataComponentTypes.POWERS, ItemPowersComponent.builder(itemPowers)
                .add(slots(), power.getId(), hidden(), negative())
                .build());

        });

        return stack;

    }

    public EnumSet<EquipmentSlotGroup> slots() {
        return slots;
    }

    public PowerReference power() {
        return power;
    }

    public boolean hidden() {
        return hidden;
    }

    public boolean negative() {
        return negative;
    }

}
