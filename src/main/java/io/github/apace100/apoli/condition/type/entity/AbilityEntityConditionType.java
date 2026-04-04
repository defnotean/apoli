package io.github.apace100.apoli.condition.type.entity;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.context.EntityConditionContext;
import io.github.apace100.apoli.condition.type.EntityConditionType;
import io.github.apace100.apoli.condition.type.EntityConditionTypes;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.ladysnake.pal.PlayerAbility;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

public class AbilityEntityConditionType extends EntityConditionType {

    public static final TypedDataObjectFactory<AbilityEntityConditionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("ability", ApoliDataTypes.PLAYER_ABILITY),
        data -> new AbilityEntityConditionType(
            data.get("ability")
        ),
        (conditionType, serializableData) -> serializableData.instance()
            .set("ability", conditionType.ability)
    );

    private final PlayerAbility ability;

    public AbilityEntityConditionType(PlayerAbility ability) {
        this.ability = ability;
    }

    @Override
    public boolean test(EntityConditionContext context) {
        return context.entity() instanceof ServerPlayer serverPlayer
            && ability.isEnabledFor(serverPlayer);
    }

    @Override
    public @NotNull ConditionConfiguration<?> getConfig() {
        return EntityConditionTypes.ABILITY;
    }

}
