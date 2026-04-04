package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.access.SlotState;
import net.minecraft.world.inventory.Slot;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.Optional;

@Mixin(Slot.class)
public abstract class SlotMixin implements SlotState {

    @Unique
    private ResourceLocation apoli$state;

    @Override
    public Optional<ResourceLocation> apoli$getState() {
        return Optional.ofNullable(apoli$state);
    }

    @Override
    public void apoli$setState(ResourceLocation state) {
        this.apoli$state = state;
    }

}
