package io.github.apace100.apoli.networking.packet.s2c;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.util.SyncStatusEffectsUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.protocol.CustomPayload;

public record SyncStatusEffectS2CPacket(int targetId, CompoundTag statusEffectData, SyncStatusEffectsUtil.UpdateType updateType) implements CustomPayload {

    public static final Id<SyncStatusEffectS2CPacket> PACKET_ID = new Id<>(Apoli.identifier("s2c/sync_status_effect"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncStatusEffectS2CPacket> PACKET_CODEC = StreamCodec.tuple(
        ByteBufCodecs.VAR_INT, SyncStatusEffectS2CPacket::targetId,
        ByteBufCodecs.UNLIMITED_NBT_COMPOUND, SyncStatusEffectS2CPacket::statusEffectData,
        SyncStatusEffectsUtil.UpdateType.PACKET_CODEC, SyncStatusEffectS2CPacket::updateType,
        SyncStatusEffectS2CPacket::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }

}
