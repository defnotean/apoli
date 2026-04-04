package io.github.apace100.apoli.data.container;

import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.util.TextAlignment;
import io.github.apace100.calio.data.CompoundSerializableDataType;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.world.Inventory;
import net.minecraft.world.MenuProvider;
import net.minecraft.resources.Identifier;

//	TODO: Finish implementation for dynamic container types -eggohito
public record DynamicContainerType(TextAlignment titleAlignment, Identifier texture, int columns, int rows) implements ContainerType {

	public static final TypedDataObjectFactory<DynamicContainerType> DATA_FACTORY = TypedDataObjectFactory.simple(
		new SerializableData()
			.add("title_alignment", ApoliDataTypes.TEXT_ALIGNMENT, TextAlignment.CENTER)
			.add("texture", SerializableDataTypes.IDENTIFIER)
			.add("columns", SerializableDataTypes.POSITIVE_INT)
			.add("rows", SerializableDataTypes.POSITIVE_INT),
		data -> new DynamicContainerType(
			data.get("title_alignment"),
			data.get("texture"),
			data.get("columns"),
			data.get("rows")
		),
		(containerType, serializableData) -> serializableData.instance()
			.set("title_alignment", containerType.titleAlignment())
			.set("texture", containerType.texture())
			.set("columns", containerType.columns())
			.set("rows", containerType.rows())
	);

	public static final CompoundSerializableDataType<DynamicContainerType> DATA_TYPE = DATA_FACTORY.getDataType();

	public DynamicContainerType {
		throw new UnsupportedOperationException("Dynamic container types are currently not supported!");
	}

	@Override
	public MenuProvider create(Inventory inventory) {
		throw new UnsupportedOperationException("Dynamic container types are currently not supported!");
	}

}
