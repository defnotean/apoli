package io.github.apace100.apoli.access;

import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

public interface SlotState {
    Optional<ResourceLocation> apoli$getState();
    void apoli$setState(ResourceLocation state);
}
