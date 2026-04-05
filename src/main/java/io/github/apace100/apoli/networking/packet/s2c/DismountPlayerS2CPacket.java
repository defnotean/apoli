package io.github.apace100.apoli.networking.packet.s2c;

import io.github.apace100.apoli.Apoli;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.protocol.CustomPayload;

public record DismountPlayerS2CPacket(int id) implements CustomPayload {

    public static final Id<DismountPlayerS2CPacket> PACKET_ID = new Id<>(Apoli.identifier("s2c/dismount_player"));
    public static final StreamCodec<FriendlyByteBuf, DismountPlayerS2CPacket> PACKET_CODEC = ByteBufCodecs.VAR_INT.xmap(DismountPlayerS2CPacket::new, DismountPlayerS2CPacket::id).cast();

    @Override
    public Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }

}
