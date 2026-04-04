package io.github.apace100.apoli.mixin;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementNode;
import net.minecraft.server.commands.AdvancementCommands;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(AdvancementCommands.class)
public interface AdvancementCommandAccessor {

    @Invoker
    static void callAddChildrenRecursivelyToList(AdvancementNode parent, List<AdvancementHolder> children) {
        throw new AssertionError();
    }

}
