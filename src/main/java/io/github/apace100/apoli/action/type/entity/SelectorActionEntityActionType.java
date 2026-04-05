package io.github.apace100.apoli.action.type.entity;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.BiEntityAction;
import io.github.apace100.apoli.action.context.EntityActionContext;
import io.github.apace100.apoli.action.type.EntityActionType;
import io.github.apace100.apoli.action.type.EntityActionTypes;
import io.github.apace100.apoli.condition.BiEntityCondition;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.util.ArgumentWrapper;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class SelectorActionEntityActionType extends EntityActionType {

    public static final TypedDataObjectFactory<SelectorActionEntityActionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("selector", ApoliDataTypes.ENTITIES_SELECTOR)
            .add("bientity_action", BiEntityAction.DATA_TYPE)
            .add("bientity_condition", BiEntityCondition.DATA_TYPE.optional(), Optional.empty()),
        data -> new SelectorActionEntityActionType(
            data.get("selector"),
            data.get("bientity_action"),
            data.get("bientity_condition")
        ),
        (actionType, serializableData) -> serializableData.instance()
            .set("selector", actionType.selector)
            .set("bientity_action", actionType.biEntityAction)
            .set("bientity_condition", actionType.biEntityCondition)
    );

    private final ArgumentWrapper<EntitySelector> selector;
    private final EntitySelector unwrappedSelector;

    private final BiEntityAction biEntityAction;
    private final Optional<BiEntityCondition> biEntityCondition;

    public SelectorActionEntityActionType(ArgumentWrapper<EntitySelector> selector, BiEntityAction biEntityAction, Optional<BiEntityCondition> biEntityCondition) {
        this.selector = selector;
        this.unwrappedSelector = selector.parsedValue();
        this.biEntityAction = biEntityAction;
        this.biEntityCondition = biEntityCondition;
    }

    @Override
    public void accept(EntityActionContext context) {

        Entity entity = context.entity();
        MinecraftServer server = entity.getServer();

        if (server == null) {
            return;
        }

        CommandSourceStack commandSource = entity.getCommandSource()
            .withOutput(CommandSource.DUMMY)
            .withLevel(Apoli.config.executeCommand.permissionLevel);

        if (Apoli.config.executeCommand.showOutput) {
            commandSource = commandSource.withOutput(entity instanceof ServerPlayer serverPlayer && serverPlayer.connection != null
                ? serverPlayer
                : server);
        }

        try {
            unwrappedSelector.getEntities(commandSource)
                .stream()
                .filter(selected -> biEntityCondition.map(condition -> condition.test(entity, selected)).orElse(true))
                .forEach(selected -> biEntityAction.execute(entity, selected));
        }

        catch (CommandSyntaxException cse) {
            commandSource.sendError(Component.literal(cse.getRawMessage()));
        }

    }

    @Override
    public @NotNull ActionConfiguration<?> getConfig() {
        return EntityActionTypes.SELECTOR_ACTION;
    }

}
