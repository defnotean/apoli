package io.github.apace100.apoli.util;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;

public final class ApoliMapCodecs {

    public static final MapCodec<Recipe<?>> RECIPE = BuiltInRegistries.RECIPE_SERIALIZER.byNameCodec().dispatchMap(Recipe::getSerializer, RecipeSerializer::codec);

}
