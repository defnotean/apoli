package io.github.apace100.apoli.networking;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.access.CustomToastViewer;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.networking.packet.VersionHandshakePacket;
import io.github.apace100.apoli.networking.packet.s2c.*;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerManager;
import io.github.apace100.apoli.power.type.ModifyTypeTagPowerType;
import io.github.apace100.apoli.power.type.PowerType;
import io.github.apace100.apoli.util.SyncStatusEffectsUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public class ModPacketsS2C {

    public static void register() {

        ClientConfigurationNetworking.registerGlobalReceiver(VersionHandshakePacket.PACKET_ID, ModPacketsS2C::sendHandshakeReply);

        ClientPlayConnectionEvents.INIT.register(((handler, client) -> {
            ClientPlayNetworking.registerReceiver(SyncPowersS2CPacket.PACKET_ID, PowerManager::receive);
            ClientPlayNetworking.registerReceiver(SyncPowerDataS2CPacket.PACKET_ID, ModPacketsS2C::onPowerSync);
            ClientPlayNetworking.registerReceiver(SyncBulkPowerDataS2CPacket.PACKET_ID, ModPacketsS2C::onPowerSyncInBulk);
            ClientPlayNetworking.registerReceiver(MountPlayerS2CPacket.PACKET_ID, ModPacketsS2C::onPlayerMount);
            ClientPlayNetworking.registerReceiver(DismountPlayerS2CPacket.PACKET_ID, ModPacketsS2C::onPlayerDismount);
            ClientPlayNetworking.registerReceiver(SyncAttackerS2CPacket.PACKET_ID, ModPacketsS2C::onAttackerSync);
            ClientPlayNetworking.registerReceiver(SyncStatusEffectS2CPacket.PACKET_ID, ModPacketsS2C::onStatusEffectSync);
            ClientPlayNetworking.registerReceiver(ShowToastS2CPacket.PACKET_ID, ModPacketsS2C::onShowToast);
            ClientPlayNetworking.registerReceiver(SyncEntityTypeTagCacheS2CPacket.PACKET_ID, ModifyTypeTagPowerType::receiveTagCache);
        }));

    }

    private static void sendHandshakeReply(VersionHandshakePacket packet, ClientConfigurationNetworking.Context context) {
        context.responseSender().send(new VersionHandshakePacket(Apoli.SEMVER));
    }

    private static void onStatusEffectSync(SyncStatusEffectS2CPacket payload, ClientPlayNetworking.Context context) {

        LocalPlayer player = context.player();

        Entity target = player.connection.level().getEntityById(payload.targetId());
        SyncStatusEffectsUtil.UpdateType updateType = payload.updateType();

        if (target instanceof LivingEntity livingTarget) {

            MobEffectInstance statusEffectInstance = updateType != SyncStatusEffectsUtil.UpdateType.CLEAR
                ? MobEffectInstance.load(payload.statusEffectData())
                : null;

            updateType.accept(livingTarget, statusEffectInstance);

        }

        else {
            Apoli.LOGGER.warn("Received packet for syncing status effect of {} entity!", (target == null ? "an unknown" : "a non-living"));
        }

    }

    private static void onAttackerSync(SyncAttackerS2CPacket payload, ClientPlayNetworking.Context context) {

        Entity target = context.player().connection.level().getEntityById(payload.targetId());
        if (!(target instanceof LivingEntity livingTarget)) {
            Apoli.LOGGER.warn("Received packet for syncing the attacker of {} entity!", (target == null ? "an unknown" : "a non-living"));
            return;
        }

        Optional<Integer> attackerId = payload.attackerId();
        if (attackerId.isEmpty()) {
            livingTarget.setAttacker(null);
            return;
        }

        Entity attacker = context.player().connection.level().getEntityById(attackerId.get());
        if (!(attacker instanceof LivingEntity livingAttacker)) {
            Apoli.LOGGER.warn("Received packet for syncing non-living attacker of entity \"{}\"!", target.getName().getString());
            return;
        }

        livingTarget.setAttacker(livingAttacker);

    }

    private static void onPlayerMount(MountPlayerS2CPacket packet, ClientPlayNetworking.Context context) {

        ClientPacketListener handler = context.player().connection;

        Entity actor = handler.level().getEntityById(packet.actorId());
        Entity target = handler.level().getEntityById(packet.targetId());

        if (target == null) {
            Apoli.LOGGER.warn("Received packet for passenger for unknown player!");
            return;
        }

        if (actor == null) {
            Apoli.LOGGER.warn("Received packet for unknown passenger for player {}!", target.getName().getString());
            return;
        }

        boolean result = actor.startRiding(target, true);

        Consumer<String> loggerMethod = result ? Apoli.LOGGER::info : Apoli.LOGGER::warn;
        String action = result ? " started riding " : " failed to start riding ";

        loggerMethod.accept(actor.getName().getString() + action + target.getName().getString());

    }

    private static void onPlayerDismount(DismountPlayerS2CPacket packet, ClientPlayNetworking.Context context) {

        LocalPlayer player = context.player();
        Entity dismountingEntity = player.connection.level().getEntityById(packet.id());

        if (dismountingEntity == null) {
            Apoli.LOGGER.warn("Received packet for unknown entity that tried to dismount!");
        }

        else if (dismountingEntity.getVehicle() instanceof Player) {
            dismountingEntity.dismountVehicle();
        }

    }

    private static void onPowerSync(SyncPowerDataS2CPacket payload, ClientPlayNetworking.Context context) {

        LocalPlayer player = context.player();
        ResourceLocation powerTypeId = payload.powerTypeId();

        if (!PowerManager.contains(powerTypeId)) {
            Apoli.LOGGER.warn("Received packet for syncing unknown power \"{}\"!", powerTypeId);
            return;
        }

        Entity entity = player.connection.level().getEntityById(payload.entityId());
        if (entity == null) {
            Apoli.LOGGER.warn("Received packet for syncing power \"{}\" to unknown entity!", powerTypeId);
            return;
        }

        PowerHolderComponent component = PowerHolderComponent.KEY
            .maybeGet(entity)
            .orElse(null);

        if (component == null) {
            Apoli.LOGGER.warn("Received packet for syncing power \"{}\" to entity \"{}\", which cannot hold powers!", powerTypeId, entity.getName().getString());
            return;
        }

        Power power = PowerManager.get(powerTypeId);
        PowerType powerType = component.getPowerType(power);

        if (powerType != null) {
            powerType.fromTag(payload.powerData().get("Data"));
        }

    }

    private static void onPowerSyncInBulk(SyncBulkPowerDataS2CPacket payload, ClientPlayNetworking.Context context) {

        Entity entity = context.player().level().getEntityById(payload.entityId());
        Map<ResourceLocation, Tag> powerAndData = payload.powerAndData();

        if (entity == null) {
            Apoli.LOGGER.warn("Received packet for syncing {} power(s) to unknown entity!", powerAndData.size());
            return;
        }

        PowerHolderComponent component = PowerHolderComponent.KEY.getNullable(entity);
        if (component == null) {
            Apoli.LOGGER.warn("Received packet for syncing {} power(s) to entity \"{}\", which cannot hold powers!", powerAndData.size(), entity.getName().getString());
            return;
        }

        int invalidPowers = 0;
        for (Map.Entry<ResourceLocation, Tag> entry : powerAndData.entrySet()) {

            ResourceLocation powerTypeId = entry.getKey();
            Tag powerTypeData = entry.getValue();

            if (!PowerManager.contains(powerTypeId)) {
                ++invalidPowers;
                continue;
            }

            Power power = PowerManager.get(powerTypeId);
            PowerType powerType = component.getPowerType(power);

            if (powerType != null) {
                powerType.fromTag(powerTypeData);
            }

        }

        if (invalidPowers > 0) {
            Apoli.LOGGER.warn("Received packet for syncing {} invalid power(s) to entity \"{}\"", invalidPowers, entity.getName().getString());
        }

    }

    public static void onShowToast(ShowToastS2CPacket packet, ClientPlayNetworking.Context context) {
        if (context.player() instanceof CustomToastViewer viewer) {
            viewer.apoli$showToast(packet.toastData());
        }
    }

}
