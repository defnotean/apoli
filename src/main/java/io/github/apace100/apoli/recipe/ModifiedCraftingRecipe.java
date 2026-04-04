package io.github.apace100.apoli.recipe;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.apace100.apoli.access.PowerCraftingInventory;
import io.github.apace100.apoli.access.PowerCraftingObject;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.mixin.CraftingInventoryAccessor;
import io.github.apace100.apoli.mixin.CraftingScreenHandlerAccessor;
import io.github.apace100.apoli.power.type.ModifyCraftingPowerType;
import io.github.apace100.apoli.power.type.Prioritized;
import io.github.apace100.apoli.util.InventoryUtil;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.CraftingRecipeCategory;
import net.minecraft.stats.RecipeBook;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.resources.Identifier;
import net.minecraft.core.NonNullList;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Optional;

public record ModifiedCraftingRecipe(Identifier id, CraftingRecipe delegate) implements CraftingRecipe {

    @Override
    public CraftingRecipeCategory getCategory() {
        return delegate().getCategory();
    }

    @Override
    public boolean matches(CraftingInput input, Level world) {
        return delegate().matches(input, world);
    }

    @Override
    public ItemStack craft(CraftingInput input, HolderLookup.Provider lookup) {

        if (input instanceof PowerCraftingInventory pci) {

            Pair<ItemStack, Collection<ModifyCraftingPowerType>> result = this.getModifiedResult(lookup, pci.apoli$getPlayer());
            pci.apoli$setPowerTypes(result.getSecond());

            return result.getFirst().copy();

        }

        else {
            return this.getResultItem(lookup).copy();
        }

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
        return ApoliRecipeSerializers.MODIFIED_CRAFTING;
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

    public Pair<ItemStack, Collection<ModifyCraftingPowerType>> getModifiedResult(HolderLookup.Provider registriesLookup, @Nullable Player player) {
        return getModifiedResult(id(), delegate(), registriesLookup, player);
    }

    public static boolean canModify(Identifier id, CraftingRecipe craftingRecipe, RecipeBook recipeBook) {
        return recipeBook instanceof PowerCraftingObject pco
            && canModify(id, craftingRecipe, pco.apoli$getPlayer());
    }

    public static boolean canModify(Identifier id, CraftingRecipe craftingRecipe, RecipeInput recipeInput) {
        return recipeInput instanceof PowerCraftingObject pco
            && canModify(id, craftingRecipe, pco.apoli$getPlayer());
    }

    public static boolean canModify(Identifier id, CraftingRecipe craftingRecipe, @Nullable Player player) {
        return player != null
            && PowerHolderComponent.hasPowerType(player, ModifyCraftingPowerType.class, mcpt -> mcpt.doesApply(id, craftingRecipe.getResultItem(player.registryAccess())));
    }

    public static Pair<ItemStack, Collection<ModifyCraftingPowerType>> getModifiedResult(Identifier id, CraftingRecipe craftingRecipe, HolderLookup.Provider registriesLookup, @Nullable Player player) {

        ItemStack resultStack = craftingRecipe.getResultItem(registriesLookup).copy();
        SlotAccess newStackRef = InventoryUtil.createStackReference(resultStack);

        Prioritized.CallInstance<ModifyCraftingPowerType> mcptpci = new Prioritized.CallInstance<>();
        mcptpci.add(player, ModifyCraftingPowerType.class, mcpt -> mcpt.doesApply(id, resultStack));

        for (int i = mcptpci.getMaxPriority(); i >= mcptpci.getMinPriority(); i--) {
            mcptpci.getPowerTypes(i).forEach(mcpt -> mcpt.getNewResult(newStackRef));
        }

        return Pair.of(newStackRef.get(), mcptpci.getAllPowerTypes());

    }

    public static Optional<BlockPos> getBlockFromInventory(TransientCraftingContainer craftingInventory) {

        if (((CraftingInventoryAccessor) craftingInventory).getHandler() instanceof CraftingMenu craftingScreenHandler) {
            return ((CraftingScreenHandlerAccessor) craftingScreenHandler).getContext().get((world, pos) -> pos);
        }

        else {
            return Optional.empty();
        }

    }

    private void send(RegistryByteBuf buf) {
        buf.writeIdentifier(id());
        Recipe.PACKET_CODEC.encode(buf, delegate());
    }

    private static ModifiedCraftingRecipe receive(RegistryByteBuf buf) {

        Identifier id = buf.readIdentifier();
        Recipe<?> recipe = Recipe.PACKET_CODEC.decode(buf);

        if (recipe instanceof CraftingRecipe craftingRecipe) {
            return new ModifiedCraftingRecipe(id, craftingRecipe);
        }

        else {
            throw new IllegalStateException("Recipe is not a crafting recipe!");
        }

    }

    public static class Serializer implements RecipeSerializer<ModifiedCraftingRecipe> {

        public static final MapCodec<ModifiedCraftingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Identifier.CODEC.fieldOf("id").forGetter(ModifiedCraftingRecipe::id),
            ApoliDataTypes.DISALLOWING_INTERNAL_CRAFTING_RECIPE.codec().fieldOf("recipe").forGetter(ModifiedCraftingRecipe::delegate)
        ).apply(instance, ModifiedCraftingRecipe::new));

        public static final PacketCodec<RegistryByteBuf, ModifiedCraftingRecipe> PACKET_CODEC = PacketCodec.of(
            ModifiedCraftingRecipe::send,
            ModifiedCraftingRecipe::receive
        );

        @Override
        public MapCodec<ModifiedCraftingRecipe> codec() {
            return CODEC;
        }

        @Override
        public PacketCodec<RegistryByteBuf, ModifiedCraftingRecipe> packetCodec() {
            return PACKET_CODEC;
        }

    }

}
