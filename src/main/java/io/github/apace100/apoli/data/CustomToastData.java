package io.github.apace100.apoli.data;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.calio.data.CompoundSerializableDataType;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import io.github.apace100.calio.registry.DataObjectFactory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public record CustomToastData(Component title, Component description, Identifier texture, ItemStack iconStack, int duration) {

    public static final Identifier DEFAULT_TEXTURE = Apoli.identifier("toast/custom");

    public static final DataObjectFactory<CustomToastData> FACTORY = DataObjectFactory.simple(
        new SerializableData()
            .add("title", SerializableDataTypes.TEXT)
            .add("description", SerializableDataTypes.TEXT)
            .add("texture", SerializableDataTypes.IDENTIFIER, DEFAULT_TEXTURE)
            .add("icon", SerializableDataTypes.ITEM_STACK, ItemStack.EMPTY)
            .add("duration", SerializableDataTypes.POSITIVE_INT, 100),
        data -> new CustomToastData(
            data.get("title"),
            data.get("description"),
            data.get("texture"),
            data.get("icon"),
            data.get("duration")
        ),
        (customToastData, serializableData) -> serializableData.instance()
            .set("title", customToastData.title())
            .set("description", customToastData.description())
            .set("texture", customToastData.texture())
            .set("icon", customToastData.iconStack())
            .set("duration", customToastData.duration())
    );

    public static final CompoundSerializableDataType<CustomToastData> DATA_TYPE = SerializableDataType.compound(FACTORY);

}
