package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.access.EndRespawningEntity;
import io.github.apace100.apoli.power.type.ActionOnItemUsePowerType;
import io.github.apace100.apoli.util.PriorityPhase;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.network.protocol.game.ClientStatusC2SPacket;
import net.minecraft.network.protocol.game.PlayerActionC2SPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerPlayNetworkHandlerMixin {

    @Shadow
    public ServerPlayer player;

    @Inject(method = "handleClientCommand", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerList;respawnPlayer(Lnet/minecraft/server/network/ServerPlayer;ZLnet/minecraft/world/entity/Entity$RemovalReason;)Lnet/minecraft/server/network/ServerPlayer;", ordinal = 0))
    private void saveEndRespawnStatus(ClientStatusC2SPacket packet, CallbackInfo ci) {
        ((EndRespawningEntity)this.player).apoli$setEndRespawning(true);
    }

    @Inject(method = "handleClientCommand", at = @At(value = "INVOKE", target = "Lnet/minecraft/advancements/criterion/ChangedDimensionTrigger;trigger(Lnet/minecraft/server/network/ServerPlayer;Lnet/minecraft/core/ResourceKey;Lnet/minecraft/core/ResourceKey;)V"))
    private void undoEndRespawnStatus(ClientStatusC2SPacket packet, CallbackInfo ci) {
        ((EndRespawningEntity)this.player).apoli$setEndRespawning(false);
    }

    @Inject(method = "handleSetCarriedItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/game/ServerboundSetCarriedItemPacket;getSlot()I", ordinal = 0))
    private void callActionOnUseStopBySwitching(ServerboundSetCarriedItemPacket packet, CallbackInfo ci) {
        if(player.isUsingItem()) {
            ActionOnItemUsePowerType.executeActions(player, SlotAccess.of(player.getInventory(), this.player.getInventory().selectedSlot), player.getUseItem(), ActionOnItemUsePowerType.TriggerType.STOP, PriorityPhase.ALL);
        }
    }

    @Inject(method = "onPlayerAction", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayer;stopUsingItem()V"))
    private void callActionOnUseStopBySwappingHands(PlayerActionC2SPacket packet, CallbackInfo ci) {
        if(player.isUsingItem()) {
            ActionOnItemUsePowerType.executeActions(player, SlotAccess.of(player.getInventory(), this.player.getInventory().selectedSlot), player.getUseItem(), ActionOnItemUsePowerType.TriggerType.STOP, PriorityPhase.ALL);
        }
    }
}
