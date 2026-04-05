package io.github.apace100.apoli.action.type.block;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.context.BlockActionContext;
import io.github.apace100.apoli.action.type.BlockActionType;
import io.github.apace100.apoli.action.type.BlockActionTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.server.MinecraftServer;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.permissions.LevelBasedPermissionSet;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec2;
import org.jetbrains.annotations.NotNull;

public class ExecuteCommandBlockActionType extends BlockActionType {

    public static final TypedDataObjectFactory<ExecuteCommandBlockActionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("command", SerializableDataTypes.STRING),
        data -> new ExecuteCommandBlockActionType(
            data.get("command")
        ),
        (actionType, serializableData) -> serializableData.instance()
            .set("command", actionType.command)
    );

    private final String command;

    public ExecuteCommandBlockActionType(String command) {
        this.command = command;
    }

    @Override
    public void accept(BlockActionContext context) {

        ServerLevel world = context.world();
        BlockPos pos = context.pos();

        BlockState blockState = world.getBlockState(pos);
        String blockDescriptionId = blockState.getBlock().getDescriptionId();

        MinecraftServer server = world.getServer();
        CommandSourceStack commandSource = new CommandSourceStack(
            Apoli.config.executeCommand.showOutput ? server : CommandSource.NULL,
            pos.getCenter(),
            Vec2.ZERO,
            world,
            LevelBasedPermissionSet.forLevel(PermissionLevel.byId(Apoli.config.executeCommand.permissionLevel)),
            blockDescriptionId,
            Component.translatable(blockDescriptionId),
            server,
            null
        );

        server.getCommands().performPrefixedCommand(commandSource, command);

    }

    @Override
    public @NotNull ActionConfiguration<?> getConfig() {
        return BlockActionTypes.EXECUTE_COMMAND;
    }

}
