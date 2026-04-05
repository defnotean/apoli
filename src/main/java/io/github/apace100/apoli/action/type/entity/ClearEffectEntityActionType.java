package io.github.apace100.apoli.action.type.entity;

import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.context.EntityActionContext;
import io.github.apace100.apoli.action.type.EntityActionType;
import io.github.apace100.apoli.action.type.EntityActionTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.util.MiscUtil;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffect;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ClearEffectEntityActionType extends EntityActionType {

    public static final TypedDataObjectFactory<ClearEffectEntityActionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("effect", SerializableDataTypes.STATUS_EFFECT, null)
            .addFunctionedDefault("effects", SerializableDataTypes.STATUS_EFFECTS, data -> MiscUtil.singletonListOrEmpty(data.get("effect"))),
        data -> new ClearEffectEntityActionType(
            data.get("effects")
        ),
        (actionType, serializableData) -> serializableData.instance()
            .set("effects", actionType.effects)
    );

    private final List<MobEffect> effects;

    public ClearEffectEntityActionType(List<MobEffect> effects) {
        this.effects = effects;
    }

    @Override
    public void accept(EntityActionContext context) {

        if (context.entity() instanceof LivingEntity livingEntity) {

            if (effects.isEmpty()) {
                livingEntity.removeAllEffects();
            }

            else {
                effects.forEach(effect -> livingEntity.removeEffect(Holder.direct(effect)));
            }

        }

    }

    @Override
    public @NotNull ActionConfiguration<?> getConfig() {
        return EntityActionTypes.CLEAR_EFFECT;
    }

}
