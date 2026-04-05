package io.github.apace100.apoli.mixin;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.client.multiplayer.ClientAdvancements;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(ClientAdvancements.class)
public interface ClientAdvancementManagerAccessor {

    // MC 26.1: field renamed from 'advancementProgresses' to 'progress'
    @Accessor("progress")
    Map<AdvancementHolder, AdvancementProgress> getAdvancementProgresses();

}
