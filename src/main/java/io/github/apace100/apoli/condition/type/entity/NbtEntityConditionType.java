package io.github.apace100.apoli.condition.type.entity;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.context.EntityConditionContext;
import io.github.apace100.apoli.condition.type.EntityConditionType;
import io.github.apace100.apoli.condition.type.EntityConditionTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueOutput;
import org.jetbrains.annotations.NotNull;

/**
 *  TODO: Use {@link SerializableDataTypes#NBT_PATH} for the 'nbt' parameter    -eggohito
 */
public class NbtEntityConditionType extends EntityConditionType {

    public static final TypedDataObjectFactory<NbtEntityConditionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("nbt", SerializableDataTypes.NBT),
        data -> new NbtEntityConditionType(
            data.get("nbt")
        ),
        (conditionType, serializableData) -> serializableData.instance()
            .set("nbt", conditionType.nbt)
    );

    private final CompoundTag nbt;

    public NbtEntityConditionType(CompoundTag nbt) {
        this.nbt = nbt;
    }

    @Override
    public boolean test(EntityConditionContext context) {
        TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, context.entity().level().registryAccess());
        context.entity().saveWithoutId(output);
        return NbtUtils.compareNbt(nbt, output.buildResult(), true);
    }

    @Override
    public @NotNull ConditionConfiguration<?> getConfig() {
        return EntityConditionTypes.NBT;
    }

}
