package io.github.apace100.apoli.action.type.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.context.EntityActionContext;
import io.github.apace100.apoli.action.type.EntityActionType;
import io.github.apace100.apoli.action.type.EntityActionTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.permissions.LevelBasedPermissionSet;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.world.phys.Vec2;
import org.jetbrains.annotations.NotNull;

public class ExecuteCommandEntityActionType extends EntityActionType {

    public static final TypedDataObjectFactory<ExecuteCommandEntityActionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("command", SerializableDataTypes.STRING),
        data -> new ExecuteCommandEntityActionType(
            data.get("command")
        ),
        (actionType, serializableData) -> serializableData.instance()
            .set("command", actionType.command)
    );

    private final String command;

    public ExecuteCommandEntityActionType(String command) {
        this.command = command;
    }

    @Override
    public void accept(EntityActionContext context) {

        Entity entity = context.entity();

        if (!(entity.level() instanceof ServerLevel serverWorld)) {
            return;
        }

        MinecraftServer server = serverWorld.getServer();

        CommandSource source = Apoli.config.executeCommand.showOutput
            ? (entity instanceof ServerPlayer serverPlayer && serverPlayer.connection != null ? (CommandSource) serverPlayer : server)
            : CommandSource.NULL;

        CommandSourceStack commandSource = new CommandSourceStack(
            source,
            entity.position(),
            Vec2.ZERO,
            serverWorld,
            LevelBasedPermissionSet.forLevel(PermissionLevel.byId(Apoli.config.executeCommand.permissionLevel)),
            entity.getName().getString(),
            entity.getDisplayName(),
            server,
            entity
        );

        server.getCommands().performPrefixedCommand(commandSource, command);

    }

    @Override
    public @NotNull ActionConfiguration<?> getConfig() {
        return EntityActionTypes.EXECUTE_COMMAND;
    }

}
