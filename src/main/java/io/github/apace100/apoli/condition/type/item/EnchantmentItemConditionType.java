package io.github.apace100.apoli.condition.type.item;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.context.ItemConditionContext;
import io.github.apace100.apoli.condition.type.ItemConditionType;
import io.github.apace100.apoli.condition.type.ItemConditionTypes;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.power.type.ModifyEnchantmentLevelPowerType;
import io.github.apace100.apoli.util.Comparison;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import net.minecraft.core.registries.Registries;

public class EnchantmentItemConditionType extends ItemConditionType {

    public static final TypedDataObjectFactory<EnchantmentItemConditionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("enchantment", SerializableDataTypes.ENCHANTMENT.optional(), Optional.empty())
            .add("use_modifications", SerializableDataTypes.BOOLEAN, true)
            .add("comparison", ApoliDataTypes.COMPARISON, Comparison.GREATER_THAN)
            .add("compare_to", SerializableDataTypes.INT, 0),
        data -> new EnchantmentItemConditionType(
            data.get("enchantment"),
            data.get("use_modifications"),
            data.get("comparison"),
            data.get("compare_to")
        ),
        (conditionType, serializableData) -> serializableData.instance()
            .set("enchantment", conditionType.enchantmentKey)
            .set("use_modifications", conditionType.useModifications)
            .set("comparison", conditionType.comparison)
            .set("compare_to", conditionType.compareTo)
    );

    private final Optional<ResourceKey<Enchantment>> enchantmentKey;
    private final boolean useModifications;

    private final Comparison comparison;
    private final int compareTo;

    public EnchantmentItemConditionType(Optional<ResourceKey<Enchantment>> enchantmentKey, boolean useModifications, Comparison comparison, int compareTo) {
        this.enchantmentKey = enchantmentKey;
        this.useModifications = useModifications;
        this.comparison = comparison;
        this.compareTo = compareTo;
    }

    @Override
    public boolean test(ItemConditionContext context) {

        ItemStack stack = context.stack();
        Level world = context.world();

        ItemEnchantments enchantmentsComponent = ModifyEnchantmentLevelPowerType.getEnchantments(stack, stack.getEnchantments(), useModifications);
        int levelOrEnchantments = enchantmentKey
            .map(key -> world.registryAccess().get(Registries.ENCHANTMENT).entryOf(key))
            .map(enchantmentsComponent::getLevel)
            .orElseGet(enchantmentsComponent::getSize);

        return comparison.compare(levelOrEnchantments, compareTo);

    }

    @Override
    public @NotNull ConditionConfiguration<?> getConfig() {
        return ItemConditionTypes.ENCHANTMENT;
    }

}
