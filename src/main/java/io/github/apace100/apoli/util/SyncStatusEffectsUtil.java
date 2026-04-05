package io.github.apace100.apoli.util;

import io.github.apace100.apoli.networking.packet.s2c.SyncStatusEffectS2CPacket;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.BiConsumer;

public class SyncStatusEffectsUtil {

    public static void sendStatusEffectUpdatePacket(LivingEntity entity, UpdateType updateType, MobEffectInstance instance) {

        if (entity.level().isClientSide()) {
            return;
        }

        CompoundTag statusEffectNbt = new CompoundTag();
        if (instance != null && updateType != UpdateType.CLEAR) {
            statusEffectNbt = (CompoundTag) MobEffectInstance.CODEC.encodeStart(net.minecraft.nbt.NbtOps.INSTANCE, instance).result().orElse(new CompoundTag());
        }

        SyncStatusEffectS2CPacket syncStatusEffectPacket = new SyncStatusEffectS2CPacket(entity.getId(), statusEffectNbt, updateType);
        for (ServerPlayer player : PlayerLookup.tracking(entity)) {
            ServerPlayNetworking.send(player, syncStatusEffectPacket);
        }

    }

    public enum UpdateType {

        CLEAR((le, sei) -> le.getActiveEffectsMap().clear()),
        APPLY((le, sei) -> {

            if (sei != null) {
                le.getActiveEffectsMap().put(sei.getEffect(), sei);
            }

        }),
        UPGRADE(APPLY::accept),
        REMOVE((le, sei) -> {

            if (sei != null) {
                le.getActiveEffectsMap().remove(sei.getEffect());
            }

        });

        public static final StreamCodec<FriendlyByteBuf, UpdateType> PACKET_CODEC = ByteBufCodecs.VAR_INT.map(index -> values()[index], UpdateType::ordinal).cast();

        final BiConsumer<LivingEntity, MobEffectInstance> consumer;
        UpdateType(BiConsumer<LivingEntity, MobEffectInstance> consumer) {
            this.consumer = consumer;
        }

        public void accept(LivingEntity le, MobEffectInstance sei) {
            this.consumer.accept(le, sei);
        }

    }

}
