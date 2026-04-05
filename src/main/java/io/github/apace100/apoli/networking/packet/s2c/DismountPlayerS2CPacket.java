package io.github.apace100.apoli.networking.packet.s2c;

import io.github.apace100.apoli.Apoli;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record DismountPlayerS2CPacket(int id) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<DismountPlayerS2CPacket> PACKET_ID = new CustomPacketPayload.Type<>(Apoli.identifier("s2c/dismount_player"));
    public static final StreamCodec<FriendlyByteBuf, DismountPlayerS2CPacket> PACKET_CODEC = ByteBufCodecs.VAR_INT.map(DismountPlayerS2CPacket::new, DismountPlayerS2CPacket::id).cast();

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return PACKET_ID;
    }

}
