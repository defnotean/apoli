package io.github.apace100.apoli.power;

import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.power.type.PowerType;
import io.github.apace100.apoli.util.TypeConfiguration;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public record PowerConfiguration<T extends PowerType>(ResourceLocation id, TypedDataObjectFactory<T> dataFactory) implements TypeConfiguration<T> {

	public static <T extends PowerType> PowerConfiguration<T> of(ResourceLocation id, SerializableData serializableData, Function<SerializableData.Instance, T> fromData, BiFunction<T, SerializableData, SerializableData.Instance> toData) {
		return of(id, TypedDataObjectFactory.simple(serializableData, fromData, toData));
	}

	public static <T extends PowerType> PowerConfiguration<T> conditionedOf(ResourceLocation id, SerializableData serializableData, BiFunction<SerializableData.Instance, Optional<EntityCondition>,  T> fromData, BiFunction<T, SerializableData, SerializableData.Instance> toData) {
		return of(id, PowerType.createConditionedDataFactory(serializableData, fromData, toData));
	}

	/**
	 * 	<b>Use {@link #of(ResourceLocation, TypedDataObjectFactory)} instead.</b>
	 */
	@Deprecated(forRemoval = true)
	public static <T extends PowerType> PowerConfiguration<T> dataFactory(ResourceLocation id, TypedDataObjectFactory<T> dataFactory) {
		return of(id, dataFactory);
	}

	public static <T extends PowerType> PowerConfiguration<T> of(ResourceLocation id, TypedDataObjectFactory<T> dataFactory) {
		return new PowerConfiguration<>(id, dataFactory);
	}

	public static <T extends PowerType> PowerConfiguration<T> simple(ResourceLocation id, Supplier<T> constructor) {
		return of(id, new SerializableData(), data -> constructor.get(), (t, serializableData) -> serializableData.instance());
	}

	public static <T extends PowerType> PowerConfiguration<T> conditionedSimple(ResourceLocation id, Function<Optional<EntityCondition>, T> constructor) {
		return conditionedOf(id, new SerializableData(), (data, entityCondition) -> constructor.apply(entityCondition), (t, serializableData) -> serializableData.instance());
	}

}
