package io.github.apace100.apoli.action.type.bientity;

import com.mojang.serialization.DataResult;
import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.context.BiEntityActionContext;
import io.github.apace100.apoli.action.type.BiEntityActionType;
import io.github.apace100.apoli.action.type.BiEntityActionTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.util.MiscUtil;
import io.github.apace100.apoli.util.modifier.Modifier;
import io.github.apace100.apoli.util.modifier.ModifierUtil;
import io.github.apace100.apoli.util.requirement.BiEntityRequirement;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.resources.ResourceKey;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class DamageBiEntityActionType extends BiEntityActionType {

    public static final TypedDataObjectFactory<DamageBiEntityActionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("damage_type", SerializableDataTypes.DAMAGE_TYPE)
            .add("amount", SerializableDataTypes.FLOAT.optional(), Optional.empty())
            .add("modifier", Modifier.DATA_TYPE, null)
            .addFunctionedDefault("modifiers", Modifier.LIST_TYPE, data -> MiscUtil.singletonListOrNull(data.get("modifier")))
            .validate(data -> {

                if (MiscUtil.anyPresent(data, "modifier", "modifiers")) {
                    return DataResult.success(data);
                }

                else {
                    Optional<Float> amount = data.get("amount");
                    return amount
                        .map(value -> DataResult.success(data))
                        .orElseGet(() -> DataResult.error(() -> "Any of 'amount', 'modifier', or 'modifiers' fields must be defined!"));
                }

            }),
        data -> new DamageBiEntityActionType(
            data.get("damage_type"),
            data.get("amount"),
            data.get("modifiers")
        ),
        (actionType, serializableData) -> serializableData.instance()
            .set("damage_type", actionType.damageType)
            .set("amount", actionType.amount)
            .set("modifiers", actionType.modifiers)
    );

    private final ResourceKey<DamageType> damageType;
    private final Optional<Float> amount;

    private final List<Modifier> modifiers;

    public DamageBiEntityActionType(ResourceKey<DamageType> damageType, Optional<Float> amount, List<Modifier> modifiers) {
        this.damageType = damageType;
        this.amount = amount;
        this.modifiers = modifiers;
    }

    @Override
    public void accept(BiEntityActionContext context) {

        Entity actor = context.actor();
        Entity target = context.target();

        this.amount
            .or(() -> getModifiedAmount(actor, target))
            .ifPresent(amount -> target.hurt(actor.damageSources().create(damageType, actor), amount));

    }

    @Override
    public @NotNull ActionConfiguration<?> getConfig() {
        return BiEntityActionTypes.DAMAGE;
    }

    @Override
    public BiEntityRequirement getRequirement() {
        return BiEntityRequirement.BOTH;
    }

    private Optional<Float> getModifiedAmount(Entity actor, Entity target) {
        return !modifiers.isEmpty() && target instanceof LivingEntity livingTarget
            ? Optional.of((float) ModifierUtil.applyModifiers(actor, modifiers, livingTarget.getMaxHealth()))
            : Optional.empty();
    }

}
