package io.github.apace100.apoli.networking.task;

import io.github.apace100.apoli.networking.packet.VersionHandshakePacket;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationNetworking;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.network.ConfigurationTask;

import java.util.function.Consumer;

public record VersionHandshakeTask(int[] semver) implements ConfigurationTask {

    public static final ConfigurationTask.Type KEY = new ConfigurationTask.Type("apoli:handshake/version");

    @Override
    public void start(Consumer<Packet<?>> sender) {
        sender.accept(ServerConfigurationNetworking.createClientboundPacket(new VersionHandshakePacket(semver)));
    }

    @Override
    public Type type() {
        return KEY;
    }

}
