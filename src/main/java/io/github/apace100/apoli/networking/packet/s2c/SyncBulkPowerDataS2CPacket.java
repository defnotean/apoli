package io.github.apace100.apoli.networking.packet.s2c;

import io.github.apace100.apoli.Apoli;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.protocol.CustomPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public record SyncBulkPowerDataS2CPacket(int entityId, Map<ResourceLocation, Tag> powerAndData) implements CustomPayload {

    public static final Id<SyncBulkPowerDataS2CPacket> PACKET_ID = new Id<>(Apoli.identifier("s2c/sync_bulk_power_data"));
    public static final PacketCodec<FriendlyByteBuf, SyncBulkPowerDataS2CPacket> PACKET_CODEC = PacketCodec.tuple(
        PacketCodecs.VAR_INT, SyncBulkPowerDataS2CPacket::entityId,
        PacketCodecs.map(HashMap::new, ResourceLocation.PACKET_CODEC, PacketCodecs.NBT_ELEMENT), SyncBulkPowerDataS2CPacket::powerAndData,
        SyncBulkPowerDataS2CPacket::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }

}
