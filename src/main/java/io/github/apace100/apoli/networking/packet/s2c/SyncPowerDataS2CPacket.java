package io.github.apace100.apoli.networking.packet.s2c;

import io.github.apace100.apoli.Apoli;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record SyncPowerDataS2CPacket(int entityId, Identifier powerTypeId, CompoundTag powerData) implements CustomPacketPayload {

    public static final CustomPacketPayload.Id<SyncPowerDataS2CPacket> PACKET_ID = new CustomPacketPayload.Id<>(Apoli.identifier("s2c/sync_power_data"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncPowerDataS2CPacket> PACKET_CODEC = StreamCodec.tuple(
        ByteBufCodecs.VAR_INT, SyncPowerDataS2CPacket::entityId,
        Identifier.PACKET_CODEC, SyncPowerDataS2CPacket::powerTypeId,
        ByteBufCodecs.NBT_COMPOUND, SyncPowerDataS2CPacket::powerData,
        SyncPowerDataS2CPacket::new
    );

    @Override
    public CustomPacketPayload.Id<? extends CustomPacketPayload> getId() {
        return PACKET_ID;
    }

}
