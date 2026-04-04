package io.github.apace100.apoli.mixin;

import net.minecraft.world.SlotRange;
import net.minecraft.world.SlotRanges;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(SlotRanges.class)
public interface SlotRangesAccessor {

    @Accessor("SLOT_RANGES")
    static List<SlotRange> getSlotRanges() {
        throw new AssertionError();
    }

}
