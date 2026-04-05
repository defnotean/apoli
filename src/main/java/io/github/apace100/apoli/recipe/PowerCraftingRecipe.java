package io.github.apace100.apoli.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.apace100.apoli.access.PowerCraftingObject;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.PowerManager;
import io.github.apace100.apoli.power.type.RecipePowerType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;

import java.util.Objects;

public class PowerCraftingRecipe implements CraftingRecipe {
    private final Identifier powerId;
    private final CraftingRecipe delegate;

    public PowerCraftingRecipe(Identifier powerId, CraftingRecipe delegate) {
        this.powerId = powerId;
        this.delegate = delegate;
    }

    public Identifier powerId() { return powerId; }
    public CraftingRecipe delegate() { return delegate; }

    @Override
    public CraftingBookCategory category() {
        return delegate().category();
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
            .map(entry -> delegate().test(input, world))
            .orElse(false);

    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        return delegate().assemble(input);
    }

    @Override
    public RecipeSerializer<PowerCraftingRecipe> getSerializer() {
        return ApoliRecipeSerializers.POWER_CRAFTING;
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

    private void send(RegistryFriendlyByteBuf buf) {
        buf.writeIdentifier(powerId());
        Recipe.STREAM_CODEC.encode(buf, delegate());
    }

    private static PowerCraftingRecipe receive(RegistryFriendlyByteBuf buf) {

        Identifier powerId = buf.readIdentifier();
        Recipe<?> recipe = Recipe.STREAM_CODEC.decode(buf);

        if (recipe instanceof CraftingRecipe craftingRecipe) {
            return new PowerCraftingRecipe(powerId, craftingRecipe);
        }

        else {
            throw new IllegalStateException("Recipe is not a crafting recipe!");
        }

    }

    public static final MapCodec<PowerCraftingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        Identifier.CODEC.fieldOf("power").forGetter(PowerCraftingRecipe::powerId),
        ApoliDataTypes.DISALLOWING_INTERNAL_CRAFTING_RECIPE.codec().fieldOf("recipe").forGetter(PowerCraftingRecipe::delegate)
    ).apply(instance, PowerCraftingRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, PowerCraftingRecipe> PACKET_CODEC = StreamCodec.of(
        PowerCraftingRecipe::send,
        PowerCraftingRecipe::receive
    );

    public static RecipeSerializer<PowerCraftingRecipe> createSerializer() {
        return new RecipeSerializer<>(CODEC, PACKET_CODEC);
    }

}
