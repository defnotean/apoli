package io.github.apace100.apoli.networking.packet.s2c;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.CustomToastData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ShowToastS2CPacket(CustomToastData toastData) implements CustomPacketPayload {

    public static final CustomPacketPayload.Id<ShowToastS2CPacket> PACKET_ID = new CustomPacketPayload.Id<>(Apoli.identifier("s2c/show_toast"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ShowToastS2CPacket> PACKET_CODEC = StreamCodec.ofStatic(
        (buf, packet) -> CustomToastData.DATA_TYPE.send(buf, packet.toastData()),
        buf -> new ShowToastS2CPacket(CustomToastData.DATA_TYPE.receive(buf))
    );

    @Override
    public CustomPacketPayload.Id<? extends CustomPacketPayload> getId() {
        return PACKET_ID;
    }

}
