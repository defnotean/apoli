package io.github.apace100.apoli.mixin;

import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AbstractContainerMenu.class)
public interface ScreenHandlerAccessor {

    @Invoker
    SlotAccess callGetCursorStackReference();

}
