package io.github.apace100.apoli.networking;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.networking.packet.VersionHandshakePacket;
import io.github.apace100.apoli.networking.packet.c2s.UseActivePowerTypesC2SPacket;
import io.github.apace100.apoli.networking.task.VersionHandshakeTask;
import io.github.apace100.apoli.power.PowerManager;
import io.github.apace100.apoli.power.type.Active;
import io.github.apace100.apoli.power.type.PowerType;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class ModPacketsC2S {

    public static void register() {

        if (Apoli.PERFORM_VERSION_CHECK) {
            ServerConfigurationConnectionEvents.CONFIGURE.register(ModPacketsC2S::handshake);
            ServerConfigurationNetworking.registerGlobalReceiver(VersionHandshakePacket.PACKET_ID, ModPacketsC2S::handleHandshakeReply);
        }

        ServerPlayConnectionEvents.INIT.register((handler, server) ->
            ServerPlayNetworking.registerReceiver(handler, UseActivePowerTypesC2SPacket.PACKET_ID, ModPacketsC2S::onUseActivePowers)
        );

    }

    private static void handshake(ServerConfigurationPacketListenerImpl handler, MinecraftServer server) {

        if (ServerConfigurationNetworking.canSend(handler, VersionHandshakePacket.PACKET_ID)) {
            handler.addTask(new VersionHandshakeTask(Apoli.SEMVER));
        }

        else {
            handler.disconnect(Component.literal("This server requires you to install the Apoli mod (v" + Apoli.VERSION + ") to play."));
        }

    }


    private static void handleHandshakeReply(VersionHandshakePacket payload, ServerConfigurationNetworking.Context context) {

        ServerConfigurationPacketListenerImpl handler = context.packetListener();

        int[] semver = payload.semver();
        boolean mismatch = semver.length != Apoli.SEMVER.length;

        for (int i = 0; !mismatch && i < semver.length - 1; i++) {

            if (semver[i] != Apoli.SEMVER[i]) {
                mismatch = true;
                break;
            }

        }

        if (!mismatch) {
            handler.completeTask(VersionHandshakeTask.KEY);
            return;
        }

        StringBuilder semverString = new StringBuilder();
        String separator = "";

        for (int i : payload.semver()) {
            semverString.append(separator).append(i);
            separator = ".";
        }

        handler.disconnect(Component.translatableEscape("apoli.gui.version_mismatch", Apoli.VERSION, semverString));

    }


    private static void onUseActivePowers(UseActivePowerTypesC2SPacket payload, ServerPlayNetworking.Context context) {

        ServerPlayer player = context.player();
        PowerHolderComponent component = PowerHolderComponent.KEY.get(player);

        for (Identifier powerId : payload.powerIds()) {

            PowerType powerType = PowerManager.getOptional(powerId)
                .map(component::getPowerType)
                .orElse(null);

            if (powerType instanceof Active activePowerType) {

                if (activePowerType.canTrigger()) {
                    activePowerType.onUse();
                }

            }

            else if (powerType != null) {
                Apoli.LOGGER.warn("Unexpectedly found power \"{}\" (which doesn't have an active power type) while receiving packet for triggering active power types of player {}!", powerId, player.getName().getString());
            }

            else {
                Apoli.LOGGER.warn("Found unknown power \"{}\" while receiving packet for triggering active power types of player {}!", powerId, player.getName().getString());
            }

        }

    }

}
