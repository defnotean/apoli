package io.github.apace100.apoli.networking.packet.s2c;

import io.github.apace100.apoli.Apoli;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;

public record SyncBulkPowerDataS2CPacket(int entityId, Map<Identifier, Tag> powerAndData) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SyncBulkPowerDataS2CPacket> PACKET_ID = new CustomPacketPayload.Type<>(Apoli.identifier("s2c/sync_bulk_power_data"));
    public static final StreamCodec<FriendlyByteBuf, SyncBulkPowerDataS2CPacket> PACKET_CODEC = StreamCodec.tuple(
        ByteBufCodecs.VAR_INT, SyncBulkPowerDataS2CPacket::entityId,
        ByteBufCodecs.map(HashMap::new, Identifier.PACKET_CODEC, ByteBufCodecs.NBT_ELEMENT), SyncBulkPowerDataS2CPacket::powerAndData,
        SyncBulkPowerDataS2CPacket::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return PACKET_ID;
    }

}
