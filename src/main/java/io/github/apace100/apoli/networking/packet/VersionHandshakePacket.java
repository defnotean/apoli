package io.github.apace100.apoli.networking.packet;

import io.github.apace100.apoli.Apoli;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record VersionHandshakePacket(int[] semver) implements CustomPacketPayload {

    public static final CustomPacketPayload.Id<VersionHandshakePacket> PACKET_ID = new CustomPacketPayload.Id<>(Apoli.identifier("handshake/version"));
    public static final StreamCodec<FriendlyByteBuf, VersionHandshakePacket> PACKET_CODEC = StreamCodec.of(VersionHandshakePacket::write, VersionHandshakePacket::read);

    public static VersionHandshakePacket read(FriendlyByteBuf buf) {
        return new VersionHandshakePacket(buf.readIntArray());
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeIntArray(semver);
    }

    @Override
    public CustomPacketPayload.Id<? extends CustomPacketPayload> getId() {
        return PACKET_ID;
    }

}
