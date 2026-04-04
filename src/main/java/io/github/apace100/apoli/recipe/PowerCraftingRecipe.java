package io.github.apace100.apoli.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.apace100.apoli.access.PowerCraftingObject;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.PowerManager;
import io.github.apace100.apoli.power.type.RecipePowerType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.CraftingRecipeCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import net.minecraft.core.NonNullList;
import net.minecraft.world.level.Level;

import java.util.Objects;

public record PowerCraftingRecipe(Identifier powerId, CraftingRecipe delegate) implements CraftingRecipe {

    @Override
    public CraftingRecipeCategory getCategory() {
        return delegate().getCategory();
    }

    @Override
    public boolean matches(CraftingInput input, Level world) {

        if (!(input instanceof PowerCraftingObject pco)) {
            return false;
        }

        boolean matchingPowerType = PowerHolderComponent.KEY.maybeGet(pco.apoli$getPlayer())
            .flatMap(component -> PowerManager.getOptional(powerId()).map(component::getPowerType))
            .map(RecipePowerType.class::isInstance)
            .orElse(false);

        return matchingPowerType && world.getRecipeManager().get(powerId())
            .filter(entry -> Objects.equals(this, entry.value()))
            .map(entry -> delegate().matches(input, world))
            .orElse(false);

    }

    @Override
    public ItemStack craft(CraftingInput input, HolderLookup.Provider lookup) {
        return delegate().craft(input, lookup);
    }

    @Override
    public boolean fits(int width, int height) {
        return delegate().fits(width, height);
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registriesLookup) {
        return delegate().getResultItem(registriesLookup);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ApoliRecipeSerializers.POWER_CRAFTING;
    }

    @Override
    public boolean isEmpty() {
        return delegate().isEmpty();
    }

    @Override
    public NonNullList<ItemStack> getRemainder(CraftingInput input) {
        return delegate().getRemainder(input);
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return delegate().getIngredients();
    }

    @Override
    public boolean isIgnoredInRecipeBook() {
        return delegate().isIgnoredInRecipeBook();
    }

    @Override
    public boolean showNotification() {
        return delegate().showNotification();
    }

    @Override
    public String getGroup() {
        return delegate().getGroup();
    }

    private void send(RegistryByteBuf buf) {
        buf.writeIdentifier(powerId());
        Recipe.PACKET_CODEC.encode(buf, delegate());
    }

    private static PowerCraftingRecipe receive(RegistryByteBuf buf) {

        Identifier powerId = buf.readIdentifier();
        Recipe<?> recipe = Recipe.PACKET_CODEC.decode(buf);

        if (recipe instanceof CraftingRecipe craftingRecipe) {
            return new PowerCraftingRecipe(powerId, craftingRecipe);
        }

        else {
            throw new IllegalStateException("Recipe is not a crafting recipe!");
        }

    }

    public static class Serializer implements RecipeSerializer<PowerCraftingRecipe> {

        public static final MapCodec<PowerCraftingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Identifier.CODEC.fieldOf("power").forGetter(PowerCraftingRecipe::powerId),
            ApoliDataTypes.DISALLOWING_INTERNAL_CRAFTING_RECIPE.codec().fieldOf("recipe").forGetter(PowerCraftingRecipe::delegate)
        ).apply(instance, PowerCraftingRecipe::new));

        public static final PacketCodec<RegistryByteBuf, PowerCraftingRecipe> PACKET_CODEC = PacketCodec.of(
            PowerCraftingRecipe::send,
            PowerCraftingRecipe::receive
        );

        @Override
        public MapCodec<PowerCraftingRecipe> codec() {
            return CODEC;
        }

        @Override
        public PacketCodec<RegistryByteBuf, PowerCraftingRecipe> packetCodec() {
            return PACKET_CODEC;
        }

    }

}
