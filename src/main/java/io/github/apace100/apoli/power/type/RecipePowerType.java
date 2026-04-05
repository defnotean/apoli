package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.mixin.RecipeManagerAccessor;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerConfiguration;
import io.github.apace100.apoli.power.PowerManager;
import io.github.apace100.apoli.recipe.PowerCraftingRecipe;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class RecipePowerType extends PowerType implements Prioritized<RecipePowerType> {

    public static final TypedDataObjectFactory<RecipePowerType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("recipe", ApoliDataTypes.DISALLOWING_INTERNAL_CRAFTING_RECIPE)
            .add("priority", SerializableDataTypes.INT, 0),
        data -> new RecipePowerType(
            data.get("recipe"),
            data.get("priority")
        ),
        (powerType, serializableData) -> serializableData.instance()
            .set("recipe", powerType.getRecipe())
            .set("priority", powerType.getPriority())
    );

    private final CraftingRecipe recipe;
    private final int priority;

    public RecipePowerType(CraftingRecipe recipe, int priority) {
        this.recipe = recipe;
        this.priority = priority;
    }

    @Override
    public @NotNull PowerConfiguration<?> getConfig() {
        return PowerTypes.RECIPE;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    public CraftingRecipe getRecipe() {
        return recipe;
    }

    // TODO: MC 26.1 - RecipeManager no longer has a flat 'recipesById' Map.
    // Recipe registration via mixin accessor needs fundamental redesign for the new RecipeMap system.
    public static void registerPowerRecipes(ReloadableServerResources dataPackContents) {
        // Intentionally stubbed out - see TODO above
    }

}
