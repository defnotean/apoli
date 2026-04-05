package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.apace100.apoli.recipe.ModifiedCraftingRecipe;
import io.github.apace100.apoli.util.RecipeUtil;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
@Mixin(RecipeManager.class)
public abstract class RecipeManagerMixin {

    // TODO: MC 26.1 renamed getFirstMatch -> getRecipeFor, and its 4th parameter changed from RecipeHolder to ResourceKey<Recipe<?>>
    @ModifyReturnValue(method = "getRecipeFor(Lnet/minecraft/world/item/crafting/RecipeType;Lnet/minecraft/world/item/crafting/RecipeInput;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/crafting/RecipeHolder;)Ljava/util/Optional;", at = @At("RETURN"))
    private Optional<RecipeHolder<?>> apoli$modifyCraftingRecipe(Optional<RecipeHolder<?>> original, RecipeType<?> type, RecipeInput input, Level world) {
        return original.map(entry -> {

            ResourceKey<Recipe<?>> id = entry.id();
            Identifier recipeId = id.identifier();
            Recipe<?> recipe = entry.value();

            if (recipe instanceof CraftingRecipe craftingRecipe && ModifiedCraftingRecipe.canModify(recipeId, craftingRecipe, input)) {
                return new RecipeHolder<>(id, new ModifiedCraftingRecipe(recipeId, craftingRecipe));
            }

            else {
                return entry;
            }

        });
    }

    // TODO: MC 26.1 changed apply() first param from Map to RecipeMap, and RecipeHolder constructor takes ResourceKey not Identifier
    @ModifyExpressionValue(method = "apply(Lnet/minecraft/world/item/crafting/RecipeMap;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V", at = @At(value = "NEW", target = "(Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/world/item/crafting/Recipe;)Lnet/minecraft/world/item/crafting/RecipeHolder;"))
    private RecipeHolder<?> apoli$validateRecipe(RecipeHolder<?> original, @Local Recipe<?> recipe) {
        return RecipeUtil.validateRecipe(recipe)
            .map(r -> original)
            .getOrThrow();
    }

}
