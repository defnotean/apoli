package io.github.apace100.apoli.condition.type.entity;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.context.EntityConditionContext;
import io.github.apace100.apoli.condition.type.EntityConditionType;
import io.github.apace100.apoli.condition.type.EntityConditionTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootContextParameterSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.BuiltInLootContextParamSets;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import net.minecraft.core.registries.Registries;

public class PredicateEntityConditionType extends EntityConditionType {

    public static final TypedDataObjectFactory<PredicateEntityConditionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("predicate", SerializableDataTypes.PREDICATE),
        data -> new PredicateEntityConditionType(
            data.get("predicate")
        ),
        (conditionType, serializableData) -> serializableData.instance()
            .set("predicate", conditionType.predicate)
    );

    private final ResourceKey<LootItemCondition> predicate;

    public PredicateEntityConditionType(ResourceKey<LootItemCondition> predicate) {
        this.predicate = predicate;
    }

    @Override
    public boolean test(EntityConditionContext context) {

        Entity entity = context.entity();
        if (!(entity.level() instanceof ServerLevel serverWorld)) {
            return false;
        }

        LootItemCondition lootCondition = serverWorld.getServer().getReloadableRegistries()
            .registryAccess()
            .get(Registries.PREDICATE)
            .getOrThrow(predicate);
        LootContextParameterSet lootContextParameterSet = new LootContextParameterSet.Builder(serverWorld)
            .add(LootContextParams.ORIGIN, entity.position())
            .addOptional(LootContextParams.THIS_ENTITY, entity)
            .build(BuiltInLootContextParamSets.COMMAND);

        return lootCondition.test(new LootContext.Builder(lootContextParameterSet).build(Optional.empty()));

    }

    @Override
    public @NotNull ConditionConfiguration<?> getConfig() {
        return EntityConditionTypes.PREDICATE;
    }

}
