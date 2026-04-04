package io.github.apace100.apoli.condition.type.item;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.context.ItemConditionContext;
import io.github.apace100.apoli.condition.type.ItemConditionType;
import io.github.apace100.apoli.condition.type.ItemConditionTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;

public class IngredientItemConditionType extends ItemConditionType {

    public static final TypedDataObjectFactory<IngredientItemConditionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("ingredient", SerializableDataTypes.INGREDIENT),
        data -> new IngredientItemConditionType(
            data.get("ingredient")
        ),
        (conditionType, serializableData) -> serializableData.instance()
            .set("ingredient", conditionType.ingredient)
    );

    private final Ingredient ingredient;

    public IngredientItemConditionType(Ingredient ingredient) {
        this.ingredient = ingredient;
    }

    @Override
    public boolean test(ItemConditionContext context) {
        return ingredient.test(context.stack());
    }

    @Override
    public @NotNull ConditionConfiguration<?> getConfig() {
        return ItemConditionTypes.INGREDIENT;
    }

}
