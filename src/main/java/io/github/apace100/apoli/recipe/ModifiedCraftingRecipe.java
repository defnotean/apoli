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
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.stats.RecipeBook;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.resources.Identifier;
import net.minecraft.core.NonNullList;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Optional;

public class ModifiedCraftingRecipe implements CraftingRecipe {
    private final Identifier id;
    private final CraftingRecipe delegate;

    public ModifiedCraftingRecipe(Identifier id, CraftingRecipe delegate) {
        this.id = id;
        this.delegate = delegate;
    }

    public Identifier id() { return id; }
    public CraftingRecipe delegate() { return delegate; }

    @Override
    public CraftingBookCategory category() {
        return delegate().category();
    }

    @Override
    public boolean matches(CraftingInput input, Level world) {
        return delegate().matches(input, world);
    }

    @Override
    public ItemStack assemble(CraftingInput input) {

        if (input instanceof PowerCraftingInventory pci) {

            Pair<ItemStack, Collection<ModifyCraftingPowerType>> result = this.getModifiedResult(pci.apoli$getPlayer());
            pci.apoli$setPowerTypes(result.getSecond());

            return result.getFirst().copy();

        }

        else {
            return delegate().assemble(input).copy();
        }

    }

    @Override
    public RecipeSerializer<ModifiedCraftingRecipe> getSerializer() {
        return ApoliRecipeSerializers.MODIFIED_CRAFTING;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
        return delegate().getRemainingItems(input);
    }

    @Override
    public PlacementInfo placementInfo() {
        return delegate().placementInfo();
    }

    @Override
    public boolean showNotification() {
        return delegate().showNotification();
    }

    @Override
    public String group() {
        return delegate().group();
    }

    public Pair<ItemStack, Collection<ModifyCraftingPowerType>> getModifiedResult(@Nullable Player player) {
        return getModifiedResult(id(), delegate(), player);
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
            && PowerHolderComponent.hasPowerType(player, ModifyCraftingPowerType.class, mcpt -> mcpt.doesApply(id, craftingRecipe.assemble(null)));
    }

    public static Pair<ItemStack, Collection<ModifyCraftingPowerType>> getModifiedResult(Identifier id, CraftingRecipe craftingRecipe, @Nullable Player player) {

        ItemStack resultStack = craftingRecipe.assemble(null).copy();
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
            return ((CraftingScreenHandlerAccessor) craftingScreenHandler).getContext().evaluate((world, pos) -> pos);
        }

        else {
            return Optional.empty();
        }

    }

    private void send(RegistryFriendlyByteBuf buf) {
        buf.writeIdentifier(id());
        Recipe.STREAM_CODEC.encode(buf, delegate());
    }

    private static ModifiedCraftingRecipe receive(RegistryFriendlyByteBuf buf) {

        Identifier id = buf.readIdentifier();
        Recipe<?> recipe = Recipe.STREAM_CODEC.decode(buf);

        if (recipe instanceof CraftingRecipe craftingRecipe) {
            return new ModifiedCraftingRecipe(id, craftingRecipe);
        }

        else {
            throw new IllegalStateException("Recipe is not a crafting recipe!");
        }

    }

    public static final MapCodec<ModifiedCraftingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        Identifier.CODEC.fieldOf("id").forGetter(ModifiedCraftingRecipe::id),
        ApoliDataTypes.DISALLOWING_INTERNAL_CRAFTING_RECIPE.codec().fieldOf("recipe").forGetter(ModifiedCraftingRecipe::delegate)
    ).apply(instance, ModifiedCraftingRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ModifiedCraftingRecipe> PACKET_CODEC = StreamCodec.ofMember(
        ModifiedCraftingRecipe::send,
        ModifiedCraftingRecipe::receive
    );

    public static RecipeSerializer<ModifiedCraftingRecipe> createSerializer() {
        return new RecipeSerializer<>(CODEC, PACKET_CODEC);
    }

}
