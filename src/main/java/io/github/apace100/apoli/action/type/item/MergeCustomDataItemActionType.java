package io.github.apace100.apoli.action.type.item;

import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.context.ItemActionContext;
import io.github.apace100.apoli.action.type.ItemActionType;
import io.github.apace100.apoli.action.type.ItemActionTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;

public class MergeCustomDataItemActionType extends ItemActionType {

    public static final TypedDataObjectFactory<MergeCustomDataItemActionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("nbt", SerializableDataTypes.NBT),
        data -> new MergeCustomDataItemActionType(
            data.get("nbt")
        ),
        (actionType, serializableData) -> serializableData.instance()
            .set("nbt", actionType.nbt)
    );

    private final CompoundTag nbt;

    public MergeCustomDataItemActionType(CompoundTag nbt) {
        this.nbt = nbt;
    }

    @Override
    public void accept(ItemActionContext context) {
        CustomData.update(DataComponents.CUSTOM_DATA, context.stackReference().get(), oldNbt -> oldNbt.merge(nbt));
    }

    @Override
    public @NotNull ActionConfiguration<?> getConfig() {
        return ItemActionTypes.MERGE_CUSTOM_DATA;
    }

}
