package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.apace100.apoli.access.PowerCraftingObject;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.PowerManager;
import io.github.apace100.apoli.power.type.RecipePowerType;
import io.github.apace100.apoli.recipe.PowerCraftingRecipe;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeMatcher;
import net.minecraft.stats.RecipeBook;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(RecipeCollection.class)
public abstract class RecipeResultCollectionMixin {

    @ModifyExpressionValue(method = "computeCraftables", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/crafting/RecipeMatcher;match(Lnet/minecraft/world/item/crafting/Recipe;Lit/unimi/dsi/fastutil/ints/IntList;)Z"))
    private boolean apoli$accountForPowerRecipes(boolean original, RecipeMatcher recipeFinder, int gridWidth, int gridHeight, RecipeBook recipeBook, @Local RecipeHolder<?> recipeEntry) {

        if (original && recipeEntry.value() instanceof PowerCraftingRecipe pcr && recipeBook instanceof PowerCraftingObject pco && pco.apoli$getPlayer() != null) {

            ResourceLocation powerId = pcr.powerId();
            PowerHolderComponent component = PowerHolderComponent.KEY.get(pco.apoli$getPlayer());

            return PowerManager.getOptional(powerId)
                .map(component::getPowerType)
                .map(RecipePowerType.class::isInstance)
                .orElse(false);

        }

        else {
            return original;
        }

    }

}
