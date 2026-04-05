package io.github.apace100.apoli.action.type.item;

import io.github.apace100.apoli.access.EntityLinkedItemStack;
import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.context.ItemActionContext;
import io.github.apace100.apoli.action.type.ItemActionType;
import io.github.apace100.apoli.action.type.ItemActionTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.loot.context.ApoliLootContextTypes;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import net.minecraft.core.registries.Registries;

public class ModifyItemActionType extends ItemActionType {

    public static final TypedDataObjectFactory<ModifyItemActionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("modifier", SerializableDataType.registryKey(Registries.ITEM_MODIFIER)),
        data -> new ModifyItemActionType(
            data.get("modifier")
        ),
        (actionType, serializableData) -> serializableData.instance()
            .set("modifier", actionType.modifier)
    );

    private final ResourceKey<LootItemFunction> modifier;

    public ModifyItemActionType(ResourceKey<LootItemFunction> modifier) {
        this.modifier = modifier;
    }

    @Override
    public void accept(ItemActionContext context) {

        ServerLevel world = context.world();
        SlotAccess stackReference = context.stackReference();

        ItemStack oldStack = stackReference.get();
        LootItemFunction itemModifier = world.getServer().reloadableRegistries()
            .lookup()
            .lookupOrThrow(Registries.ITEM_MODIFIER)
            .getOrThrow(modifier)
            .value();

        LootParams lootContextParameterSet = new LootParams.Builder(world)
            .add(LootContextParams.ORIGIN, world.getLevelData().getRespawnData().pos().getCenter())
            .add(LootContextParams.TOOL, oldStack)
            .addOptional(LootContextParams.THIS_ENTITY, ((EntityLinkedItemStack) oldStack).apoli$getEntity())
            .build(ApoliLootContextTypes.ANY);

        ItemStack newStack = itemModifier.apply(oldStack, new LootContext.Builder(lootContextParameterSet).build(ApoliLootContextTypes.ANY));
        stackReference.set(newStack);

    }

    @Override
    public @NotNull ActionConfiguration<?> getConfig() {
        return ItemActionTypes.MODIFY;
    }

}
