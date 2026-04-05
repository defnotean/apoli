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
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.stats.RecipeBook;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(RecipeCollection.class)
public abstract class RecipeResultCollectionMixin {

    // TODO: MC 26.1 completely replaced computeCraftables() with selectRecipes() which uses
    // RecipeDisplayEntry/StackedItemContents instead of RecipeHolder/StackedContents.
    // RecipeMatcher class was also removed. Power recipe craftability needs reimplementing.
    // @ModifyExpressionValue(method = "computeCraftables", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/crafting/RecipeMatcher;match(...)Z"))
    // private boolean apoli$accountForPowerRecipes(...) { ... }

}
