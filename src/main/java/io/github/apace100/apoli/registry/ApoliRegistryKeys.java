package io.github.apace100.apoli.registry;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.type.BiEntityActionType;
import io.github.apace100.apoli.action.type.BlockActionType;
import io.github.apace100.apoli.action.type.EntityActionType;
import io.github.apace100.apoli.action.type.ItemActionType;
import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.*;
import io.github.apace100.apoli.data.container.ContainerType;
import io.github.apace100.apoli.power.PowerConfiguration;
import io.github.apace100.apoli.power.type.PowerType;
import io.github.apace100.apoli.util.modifier.IModifierOperation;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public class ApoliRegistryKeys {

    public static final ResourceKey<Registry<PowerConfiguration<PowerType>>> POWER_TYPE = create("power_type");

    public static final ResourceKey<Registry<ConditionConfiguration<BiEntityConditionType>>> BIENTITY_CONDITION_TYPE = create("bientity_condition_type");
    public static final ResourceKey<Registry<ConditionConfiguration<BiomeConditionType>>> BIOME_CONDITION_TYPE = create("biome_condition_type");
    public static final ResourceKey<Registry<ConditionConfiguration<BlockConditionType>>> BLOCK_CONDITION_TYPE = create("block_condition_type");
    public static final ResourceKey<Registry<ConditionConfiguration<DamageConditionType>>> DAMAGE_CONDITION_TYPE = create("damage_condition_type");
    public static final ResourceKey<Registry<ConditionConfiguration<EntityConditionType>>> ENTITY_CONDITION_TYPE = create("entity_condition_type");
    public static final ResourceKey<Registry<ConditionConfiguration<FluidConditionType>>> FLUID_CONDITION_TYPE = create("fluid_condition_type");
    public static final ResourceKey<Registry<ConditionConfiguration<ItemConditionType>>> ITEM_CONDITION_TYPE = create("item_condition_type");

    public static final ResourceKey<Registry<ActionConfiguration<BiEntityActionType>>> BIENTITY_ACTION_TYPE = create("bientity_action_type");
    public static final ResourceKey<Registry<ActionConfiguration<BlockActionType>>> BLOCK_ACTION_TYPE = create("block_action_type");
    public static final ResourceKey<Registry<ActionConfiguration<EntityActionType>>> ENTITY_ACTION_TYPE = create("entity_action_type");
    public static final ResourceKey<Registry<ActionConfiguration<ItemActionType>>> ITEM_ACTION_TYPE = create("item_action_type");

    public static final ResourceKey<Registry<IModifierOperation>> MODIFIER_OPERATION = create("modifier_operation");
    public static final ResourceKey<Registry<ContainerType>> CONTAINER_TYPE = create("container_type");

    private static <T> ResourceKey<Registry<T>> create(String path) {
        return ResourceKey.createRegistryKey(Apoli.identifier(path));
    }

}
