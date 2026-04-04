package io.github.apace100.apoli.action.type.item;

import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.context.ItemActionContext;
import io.github.apace100.apoli.action.type.ItemActionType;
import io.github.apace100.apoli.action.type.ItemActionTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.NotNull;

public class DamageItemActionType extends ItemActionType {

    public static final TypedDataObjectFactory<DamageItemActionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("amount", SerializableDataTypes.INT, 1)
            .add("ignore_unbreaking", SerializableDataTypes.BOOLEAN, false),
        data -> new DamageItemActionType(
            data.get("amount"),
            data.get("ignore_unbreaking")
        ),
        (actionType, serializableData) -> serializableData.instance()
            .set("amount", actionType.amount)
            .set("ignore_unbreaking", actionType.ignoreUnbreaking)
    );

    private final int amount;
    private final boolean ignoreUnbreaking;

    public DamageItemActionType(int amount, boolean ignoreUnbreaking) {
        this.amount = amount;
        this.ignoreUnbreaking = ignoreUnbreaking;
    }

    @Override
    public void accept(ItemActionContext context) {

        ServerLevel world = context.world();
        ItemStack stack = context.stackReference().get();

        if (ignoreUnbreaking) {

            if (amount >= stack.getMaxDamage()) {
                stack.decrement(1);
            }

            else {
                stack.setDamageValue(stack.getDamageValue() + amount);
            }

        }

        else {
            stack.hurt(amount, world, null, item -> {});
        }

    }

    @Override
    public @NotNull ActionConfiguration<?> getConfig() {
        return ItemActionTypes.DAMAGE;
    }

}
