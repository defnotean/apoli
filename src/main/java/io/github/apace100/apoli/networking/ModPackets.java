package io.github.apace100.apoli.networking;

import io.github.apace100.apoli.networking.packet.VersionHandshakePacket;
import io.github.apace100.apoli.networking.packet.c2s.UseActivePowerTypesC2SPacket;
import io.github.apace100.apoli.networking.packet.s2c.*;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

public class ModPackets {

    public static void register() {

        PayloadTypeRegistry.clientboundConfiguration().register(VersionHandshakePacket.PACKET_ID, VersionHandshakePacket.PACKET_CODEC);
        PayloadTypeRegistry.serverboundConfiguration().register(VersionHandshakePacket.PACKET_ID, VersionHandshakePacket.PACKET_CODEC);

        PayloadTypeRegistry.clientboundPlay().register(SyncAttackerS2CPacket.PACKET_ID, SyncAttackerS2CPacket.PACKET_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(DismountPlayerS2CPacket.PACKET_ID, DismountPlayerS2CPacket.PACKET_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(SyncStatusEffectS2CPacket.PACKET_ID, SyncStatusEffectS2CPacket.PACKET_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(ShowToastS2CPacket.PACKET_ID, ShowToastS2CPacket.PACKET_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(MountPlayerS2CPacket.PACKET_ID, MountPlayerS2CPacket.PACKET_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(SyncPowerDataS2CPacket.PACKET_ID, SyncPowerDataS2CPacket.PACKET_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(SyncBulkPowerDataS2CPacket.PACKET_ID, SyncBulkPowerDataS2CPacket.PACKET_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(SyncPowersS2CPacket.PACKET_ID, SyncPowersS2CPacket.PACKET_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(SyncEntityTypeTagCacheS2CPacket.PACKET_ID, SyncEntityTypeTagCacheS2CPacket.PACKET_CODEC);

        PayloadTypeRegistry.serverboundPlay().register(UseActivePowerTypesC2SPacket.PACKET_ID, UseActivePowerTypesC2SPacket.PACKET_CODEC);

    }

}
