package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.access.PowerCraftingObject;
import net.minecraft.world.entity.player.Player;
import net.minecraft.stats.RecipeBook;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.lang.ref.WeakReference;
import java.util.Objects;

@Mixin(RecipeBook.class)
public abstract class RecipeBookMixin implements PowerCraftingObject {

    @Unique
    private WeakReference<Player> apoli$player;

    @Override
    public Player apoli$getPlayer() {
        return Objects.requireNonNull(apoli$player.get(), "Player was cleared; recipe book: " + this);
    }

    @Override
    public void apoli$setPlayer(Player player) {
        this.apoli$player = new WeakReference<>(player);
    }

}
