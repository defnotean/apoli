package io.github.apace100.apoli.mixin;

import net.minecraft.world.entity.ai.behavior.ShufflingList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ShufflingList.Entry.class)
public interface WeightedListEntryAccessor {

    @Accessor
    int getWeight();
}
