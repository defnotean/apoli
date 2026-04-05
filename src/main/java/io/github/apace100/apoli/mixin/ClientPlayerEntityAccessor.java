package io.github.apace100.apoli.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LocalPlayer.class)
public interface ClientPlayerEntityAccessor {

    // MC 26.1: field renamed from 'client' to 'minecraft'
    @Accessor("minecraft")
    Minecraft getClient();

}
