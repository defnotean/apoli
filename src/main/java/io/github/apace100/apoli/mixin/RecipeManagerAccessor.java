package io.github.apace100.apoli.mixin;

import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(RecipeManager.class)
public interface RecipeManagerAccessor {

	// TODO: MC 26.1 - RecipeManager no longer has a 'recipesById' Map field.
	// It now uses RecipeMap with ResourceKey-based lookups via byKey().
	// This accessor and all callers need fundamental redesign.
	// @Accessor
	// Map<Identifier, RecipeHolder<?>> getRecipesById();

}
