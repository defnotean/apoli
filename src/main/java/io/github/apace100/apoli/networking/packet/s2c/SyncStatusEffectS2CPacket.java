package io.github.apace100.apoli.networking.packet.s2c;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.util.SyncStatusEffectsUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record SyncStatusEffectS2CPacket(int targetId, CompoundTag statusEffectData, SyncStatusEffectsUtil.UpdateType updateType) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SyncStatusEffectS2CPacket> PACKET_ID = new CustomPacketPayload.Type<>(Apoli.identifier("s2c/sync_status_effect"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncStatusEffectS2CPacket> PACKET_CODEC = StreamCodec.tuple(
        ByteBufCodecs.VAR_INT, SyncStatusEffectS2CPacket::targetId,
        ByteBufCodecs.UNLIMITED_NBT_COMPOUND, SyncStatusEffectS2CPacket::statusEffectData,
        SyncStatusEffectsUtil.UpdateType.PACKET_CODEC, SyncStatusEffectS2CPacket::updateType,
        SyncStatusEffectS2CPacket::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return PACKET_ID;
    }

}
