package io.github.apace100.apoli.recipe;

import io.github.apace100.apoli.Apoli;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;

public class ApoliRecipeSerializers {

    public static final RecipeSerializer<PowerCraftingRecipe> POWER_CRAFTING = register("power_crafting", PowerCraftingRecipe.createSerializer());
    public static final RecipeSerializer<ModifiedCraftingRecipe> MODIFIED_CRAFTING = register("modified_crafting", ModifiedCraftingRecipe.createSerializer());

    public static void register() {

    }

    public static <R extends Recipe<?>> RecipeSerializer<R> register(String path, RecipeSerializer<R> serializer) {
        return Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, Apoli.identifier(path), serializer);
    }

}
