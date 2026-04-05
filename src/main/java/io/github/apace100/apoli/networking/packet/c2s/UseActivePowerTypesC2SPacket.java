package io.github.apace100.apoli.networking.packet.c2s;

import io.github.apace100.apoli.Apoli;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

public record UseActivePowerTypesC2SPacket(List<Identifier> powerIds) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<UseActivePowerTypesC2SPacket> PACKET_ID = new CustomPacketPayload.Type<>(Apoli.identifier("c2s/use_active_power_types"));
    public static final StreamCodec<RegistryFriendlyByteBuf, UseActivePowerTypesC2SPacket> PACKET_CODEC = StreamCodec.composite(
        ByteBufCodecs.collection(ArrayList::new, Identifier.STREAM_CODEC), UseActivePowerTypesC2SPacket::powerIds,
        UseActivePowerTypesC2SPacket::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return PACKET_ID;
    }

}
