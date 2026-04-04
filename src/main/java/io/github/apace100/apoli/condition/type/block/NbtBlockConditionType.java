package io.github.apace100.apoli.condition.type.block;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.context.BlockConditionContext;
import io.github.apace100.apoli.condition.type.BlockConditionType;
import io.github.apace100.apoli.condition.type.BlockConditionTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import org.jetbrains.annotations.NotNull;

/**
 *  TODO: Use {@link SerializableDataTypes#NBT_PATH} for the 'nbt' parameter    -eggohito
 */
public class NbtBlockConditionType extends BlockConditionType {

    public static final TypedDataObjectFactory<NbtBlockConditionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("nbt", SerializableDataTypes.NBT_COMPOUND),
        data -> new NbtBlockConditionType(
            data.get("nbt")
        ),
        (conditionType, serializableData) -> serializableData.instance()
            .set("nbt", conditionType.nbt)
    );

    private final CompoundTag nbt;

    public NbtBlockConditionType(CompoundTag nbt) {
        this.nbt = nbt;
    }

    @Override
    public boolean test(BlockConditionContext context) {
        return context.blockEntity()
            .map(be -> be.createNbtWithIdentifyingData(context.world().registryAccess()))
            .map(beNbt -> NbtUtils.compareNbt(nbt, beNbt, true))
            .orElse(false);
    }

    @Override
    public @NotNull ConditionConfiguration<?> getConfig() {
        return BlockConditionTypes.NBT;
    }

}
