package io.github.apace100.apoli.networking.packet.s2c;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.Power;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public record SyncPowersS2CPacket(Map<Identifier, Power> powersById) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SyncPowersS2CPacket> PACKET_ID = new CustomPacketPayload.Type<>(Apoli.identifier("s2c/sync_power_registry"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncPowersS2CPacket> PACKET_CODEC = StreamCodec.ofMember(SyncPowersS2CPacket::write, SyncPowersS2CPacket::read);

    public static SyncPowersS2CPacket read(RegistryFriendlyByteBuf buf) {

        try {

            Collection<Power> powers = new ObjectArrayList<>();
            int powersCount = buf.readVarInt();

            for (int i = 0; i < powersCount; i++) {
                powers.add(Power.DATA_TYPE.receive(buf));
            }

            return new SyncPowersS2CPacket(powers
                .stream()
                .collect(Collectors.toMap(Power::getId, Function.identity(), (oldPower, newPower) -> newPower, Object2ObjectOpenHashMap::new)));

        }

        catch (Exception e) {
            Apoli.LOGGER.error(e.getMessage());
            throw e;
        }

    }

    public void write(RegistryFriendlyByteBuf buf) {

        Collection<Power> powers = powersById().values();

        buf.writeVarInt(powers.size());
        powers.forEach(power -> Power.DATA_TYPE.send(buf, power));

    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return PACKET_ID;
    }

}
