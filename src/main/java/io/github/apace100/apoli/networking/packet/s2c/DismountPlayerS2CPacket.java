package io.github.apace100.apoli.networking.packet.s2c;

import io.github.apace100.apoli.Apoli;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record DismountPlayerS2CPacket(int id) implements CustomPacketPayload {

    public static final CustomPacketPayload.Id<DismountPlayerS2CPacket> PACKET_ID = new CustomPacketPayload.Id<>(Apoli.identifier("s2c/dismount_player"));
    public static final StreamCodec<FriendlyByteBuf, DismountPlayerS2CPacket> PACKET_CODEC = ByteBufCodecs.VAR_INT.xmap(DismountPlayerS2CPacket::new, DismountPlayerS2CPacket::id).cast();

    @Override
    public CustomPacketPayload.Id<? extends CustomPacketPayload> getId() {
        return PACKET_ID;
    }

}
