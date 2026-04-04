package io.github.apace100.apoli.networking.packet.c2s;

import io.github.apace100.apoli.Apoli;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.protocol.CustomPayload;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

public record UseActivePowerTypesC2SPacket(List<Identifier> powerIds) implements CustomPayload {

    public static final Id<UseActivePowerTypesC2SPacket> PACKET_ID = new Id<>(Apoli.identifier("c2s/use_active_power_types"));
    public static final PacketCodec<RegistryByteBuf, UseActivePowerTypesC2SPacket> PACKET_CODEC = PacketCodec.tuple(
        PacketCodecs.collection(ArrayList::new, Identifier.PACKET_CODEC), UseActivePowerTypesC2SPacket::powerIds,
        UseActivePowerTypesC2SPacket::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }

}
