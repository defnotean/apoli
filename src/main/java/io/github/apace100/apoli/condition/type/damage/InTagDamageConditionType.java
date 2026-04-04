package io.github.apace100.apoli.condition.type.damage;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.context.DamageConditionContext;
import io.github.apace100.apoli.condition.type.DamageConditionType;
import io.github.apace100.apoli.condition.type.DamageConditionTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import org.jetbrains.annotations.NotNull;
import net.minecraft.core.registries.Registries;

public class InTagDamageConditionType extends DamageConditionType {

    public static final TypedDataObjectFactory<InTagDamageConditionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("tag", SerializableDataType.tagKey(Registries.DAMAGE_TYPE)),
        data -> new InTagDamageConditionType(
            data.get("tag")
        ),
        (t, serializableData) -> serializableData.instance()
            .set("tag", t.tag)
    );

    private final TagKey<DamageType> tag;

    public InTagDamageConditionType(TagKey<DamageType> tag) {
        this.tag = tag;
    }

    @Override
    public boolean test(DamageConditionContext context) {
        return context.source().is(tag);
    }

    @Override
    public @NotNull ConditionConfiguration<?> getConfig() {
        return DamageConditionTypes.IN_TAG;
    }

}
