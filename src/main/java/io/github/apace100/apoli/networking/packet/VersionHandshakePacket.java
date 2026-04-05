package io.github.apace100.apoli.networking.packet;

import io.github.apace100.apoli.Apoli;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.CustomPayload;

public record VersionHandshakePacket(int[] semver) implements CustomPayload {

    public static final Id<VersionHandshakePacket> PACKET_ID = new Id<>(Apoli.identifier("handshake/version"));
    public static final StreamCodec<FriendlyByteBuf, VersionHandshakePacket> PACKET_CODEC = StreamCodec.of(VersionHandshakePacket::write, VersionHandshakePacket::read);

    public static VersionHandshakePacket read(FriendlyByteBuf buf) {
        return new VersionHandshakePacket(buf.readIntArray());
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeIntArray(semver);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }

}
