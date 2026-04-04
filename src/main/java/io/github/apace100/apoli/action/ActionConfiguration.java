package io.github.apace100.apoli.action;

import io.github.apace100.apoli.action.type.ActionType;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.util.TypeConfiguration;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.resources.ResourceLocation;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public record ActionConfiguration<T extends ActionType<?, ?>>(ResourceLocation id, TypedDataObjectFactory<T> dataFactory) implements TypeConfiguration<T> {

	public static <T extends ActionType<?, ?>> ActionConfiguration<T> of(ResourceLocation id, SerializableData serializableData, Function<SerializableData.Instance, T> fromData, BiFunction<T, SerializableData, SerializableData.Instance> toData) {
		TypedDataObjectFactory<T> dataFactory = TypedDataObjectFactory.simple(serializableData, fromData, toData);
		return of(id, dataFactory);
	}

	public static <T extends ActionType<?, ?>> ActionConfiguration<T> of(ResourceLocation id, TypedDataObjectFactory<T> dataFactory) {
		return new ActionConfiguration<>(id, dataFactory);
	}

	public static <T extends ActionType<?, ?>> ActionConfiguration<T> simple(ResourceLocation id, Supplier<T> constructor) {
		return of(id, new SerializableData(), data -> constructor.get(), (t, serializableData) -> serializableData.instance());
	}

}
