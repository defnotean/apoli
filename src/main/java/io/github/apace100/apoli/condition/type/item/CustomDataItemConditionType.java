package io.github.apace100.apoli.condition.type.item;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.context.ItemConditionContext;
import io.github.apace100.apoli.condition.type.ItemConditionType;
import io.github.apace100.apoli.condition.type.ItemConditionTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;

public class CustomDataItemConditionType extends ItemConditionType {

    public static final TypedDataObjectFactory<CustomDataItemConditionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("nbt", SerializableDataTypes.NBT),
        data -> new CustomDataItemConditionType(
            data.get("nbt")
        ),
        (conditionType, serializableData) -> serializableData.instance()
            .set("nbt", conditionType.nbt)
    );

    private final CompoundTag nbt;

    public CustomDataItemConditionType(CompoundTag nbt) {
        this.nbt = nbt;
    }

    @Override
    public boolean test(ItemConditionContext context) {
        return context.stack().getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).matchedBy(nbt);
    }

    @Override
    public @NotNull ConditionConfiguration<?> getConfig() {
        return ItemConditionTypes.CUSTOM_DATA;
    }

}
