package io.github.apace100.apoli.action.type.item;

import io.github.apace100.apoli.access.EntityLinkedItemStack;
import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.context.ItemActionContext;
import io.github.apace100.apoli.action.type.ItemActionType;
import io.github.apace100.apoli.action.type.ItemActionTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.util.MiscUtil;
import io.github.apace100.apoli.util.modifier.Modifier;
import io.github.apace100.apoli.util.modifier.ModifierUtil;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ModifyItemCooldownItemActionType extends ItemActionType {

    public static final TypedDataObjectFactory<ModifyItemCooldownItemActionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("modifier", Modifier.DATA_TYPE, null)
            .addFunctionedDefault("modifiers", Modifier.LIST_TYPE, data -> MiscUtil.singletonListOrNull(data.get("modifier")))
            .validate(MiscUtil.validateAnyFieldsPresent("modifier", "modifiers")),
        data -> new ModifyItemCooldownItemActionType(
            data.get("modifiers")
        ),
        (actionType, serializableData) -> serializableData.instance()
            .set("modifiers", actionType.modifiers)
    );

    private final List<Modifier> modifiers;

    public ModifyItemCooldownItemActionType(List<Modifier> modifiers) {
        this.modifiers = modifiers;
    }

    @Override
    public void accept(ItemActionContext context) {

        ItemStack stack = context.stackReference().get();
        if (stack.isEmpty() || modifiers.isEmpty() || !(((EntityLinkedItemStack) stack).apoli$getEntity(true) instanceof Player player)) {
            return;
        }

        ItemCooldowns cooldownManager = player.getItemCooldowns();
        ItemCooldowns.Entry cooldownEntry = cooldownManager.entries.get(stack.getItem());

        int oldDuration = cooldownEntry != null
            ? cooldownEntry.endTick - cooldownEntry.startTick
            : 0;

        cooldownManager.set(stack.getItem(), (int) ModifierUtil.applyModifiers(player, modifiers, oldDuration));

    }

    @Override
    public @NotNull ActionConfiguration<?> getConfig() {
        return ItemActionTypes.MODIFY_ITEM_COOLDOWN;
    }

}
