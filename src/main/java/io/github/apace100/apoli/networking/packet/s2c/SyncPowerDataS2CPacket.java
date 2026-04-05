package io.github.apace100.apoli.networking.packet.s2c;

import io.github.apace100.apoli.Apoli;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record SyncPowerDataS2CPacket(int entityId, Identifier powerTypeId, CompoundTag powerData) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SyncPowerDataS2CPacket> PACKET_ID = new CustomPacketPayload.Type<>(Apoli.identifier("s2c/sync_power_data"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncPowerDataS2CPacket> PACKET_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT, SyncPowerDataS2CPacket::entityId,
        Identifier.STREAM_CODEC, SyncPowerDataS2CPacket::powerTypeId,
        ByteBufCodecs.COMPOUND_TAG, SyncPowerDataS2CPacket::powerData,
        SyncPowerDataS2CPacket::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return PACKET_ID;
    }

}
