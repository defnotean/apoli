package io.github.apace100.apoli.networking.packet.s2c;

import io.github.apace100.apoli.Apoli;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.protocol.CustomPayload;

import java.util.Optional;

public record SyncAttackerS2CPacket(int targetId, Optional<Integer> attackerId) implements CustomPayload {

    public static final Id<SyncAttackerS2CPacket> PACKET_ID = new Id<>(Apoli.identifier("s2c/sync_attacker"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncAttackerS2CPacket> PACKET_CODEC = StreamCodec.tuple(
        ByteBufCodecs.VAR_INT, SyncAttackerS2CPacket::targetId,
        ByteBufCodecs.optional(ByteBufCodecs.VAR_INT), SyncAttackerS2CPacket::attackerId,
        SyncAttackerS2CPacket::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }

}
