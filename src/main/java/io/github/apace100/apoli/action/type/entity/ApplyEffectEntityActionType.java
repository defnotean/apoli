package io.github.apace100.apoli.action.type.entity;

import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.context.EntityActionContext;
import io.github.apace100.apoli.action.type.EntityActionType;
import io.github.apace100.apoli.action.type.EntityActionTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.util.MiscUtil;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffectInstance;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ApplyEffectEntityActionType extends EntityActionType {

    public static final TypedDataObjectFactory<ApplyEffectEntityActionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("effect", SerializableDataTypes.STATUS_EFFECT_INSTANCE, null)
            .addFunctionedDefault("effects", SerializableDataTypes.STATUS_EFFECT_INSTANCES, data -> MiscUtil.singletonListOrNull(data.get("effect")))
            .validate(MiscUtil.validateAnyFieldsPresent("effect", "effects")),
        data -> new ApplyEffectEntityActionType(
            data.get("effects")
        ),
        (actionType, serializableData) -> serializableData.instance()
            .set("effects", actionType.effects)
    );

    private final List<MobEffectInstance> effects;

    public ApplyEffectEntityActionType(List<MobEffectInstance> effects) {
        this.effects = effects;
    }

    @Override
    public void accept(EntityActionContext context) {

        if (context.entity() instanceof LivingEntity livingEntity && !livingEntity.level().isClientSide()) {
            effects.forEach(livingEntity::addEffect);
        }

    }

    @Override
    public @NotNull ActionConfiguration<?> getConfig() {
        return EntityActionTypes.APPLY_EFFECT;
    }

}
