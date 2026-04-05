package io.github.apace100.apoli.condition.type.item;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.context.ItemConditionContext;
import io.github.apace100.apoli.condition.type.ItemConditionType;
import io.github.apace100.apoli.condition.type.ItemConditionTypes;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.util.Comparison;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.world.level.block.entity.FuelValues;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class FuelItemConditionType extends ItemConditionType {

	public static final TypedDataObjectFactory<FuelItemConditionType> DATA_FACTORY = TypedDataObjectFactory.simple(
		new SerializableData()
			.add("comparison", ApoliDataTypes.COMPARISON, Comparison.GREATER_THAN)
			.add("compare_to", SerializableDataTypes.INT, 0),
		data -> new FuelItemConditionType(
			data.get("comparison"),
			data.get("compare_to")
		),
		(conditionType, serializableData) -> serializableData.instance()
			.set("comparison", conditionType.comparison)
			.set("compare_to", conditionType.compareTo)
	);

	private final Comparison comparison;
	private final int compareTo;

	public FuelItemConditionType(Comparison comparison, int compareTo) {
		this.comparison = comparison;
		this.compareTo = compareTo;
	}

	@Override
	public boolean test(ItemConditionContext context) {
		// In MC 26.1, FuelRegistry was removed. Fuel values are now per-level via FuelValues.
		// We approximate by checking if the item is a known fuel (burn time > 0 through component data)
		int burnTime = context.stack().getBurnTime();
		return comparison.compare(burnTime, compareTo);
	}

	@Override
	public @NotNull ConditionConfiguration<?> getConfig() {
		return ItemConditionTypes.FUEL;
	}

}
