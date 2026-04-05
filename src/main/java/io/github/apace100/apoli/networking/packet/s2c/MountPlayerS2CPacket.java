package io.github.apace100.apoli.networking.packet.s2c;

import io.github.apace100.apoli.Apoli;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record MountPlayerS2CPacket(int actorId, int targetId) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<MountPlayerS2CPacket> PACKET_ID = new CustomPacketPayload.Type<>(Apoli.identifier("s2c/mount_player"));
    public static final StreamCodec<FriendlyByteBuf, MountPlayerS2CPacket> PACKET_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT, MountPlayerS2CPacket::actorId,
        ByteBufCodecs.VAR_INT, MountPlayerS2CPacket::targetId,
        MountPlayerS2CPacket::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return PACKET_ID;
    }

}
