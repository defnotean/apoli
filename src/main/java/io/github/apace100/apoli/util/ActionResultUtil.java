package io.github.apace100.apoli.util;

import net.minecraft.world.InteractionResult;

public class ActionResultUtil {

    public static boolean shouldOverride(InteractionResult oldResult, InteractionResult newResult) {
        return (newResult.consumesAction() && !oldResult.consumesAction())
            || (newResult instanceof InteractionResult.Success && !(oldResult instanceof InteractionResult.Success));
    }

}
