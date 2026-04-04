package io.github.apace100.apoli.action.type.entity;

import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.context.EntityActionContext;
import io.github.apace100.apoli.action.type.EntityActionType;
import io.github.apace100.apoli.action.type.EntityActionTypes;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.util.AdvancementUtil;
import io.github.apace100.apoli.util.MiscUtil;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerAdvancementLoader;
import net.minecraft.server.commands.AdvancementCommands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class RevokeAdvancementEntityActionType extends EntityActionType {

    public static final TypedDataObjectFactory<RevokeAdvancementEntityActionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("advancement", SerializableDataTypes.IDENTIFIER.optional(), Optional.empty())
            .add("selection", ApoliDataTypes.ADVANCEMENT_SELECTION, AdvancementCommands.Selection.ONLY)
            .add("criterion", SerializableDataTypes.STRING, null)
            .addFunctionedDefault("criteria", SerializableDataTypes.STRINGS, data -> MiscUtil.singletonListOrEmpty(data.get("criterion"))),
        data -> new RevokeAdvancementEntityActionType(
            data.get("advancement"),
            data.get("selection"),
            data.get("criteria")
        ),
        (actionType, serializableData) -> serializableData.instance()
            .set("advancement", actionType.advancementId)
            .set("selection", actionType.selection)
            .set("criteria", actionType.criteria)
    );

    private final Optional<Identifier> advancementId;
    private final AdvancementCommands.Selection selection;

    private final List<String> criteria;

    public RevokeAdvancementEntityActionType(Optional<Identifier> advancementId, AdvancementCommands.Selection selection, List<String> criteria) {
        this.advancementId = advancementId;
        this.selection = selection;
        this.criteria = criteria;
    }

    @Override
    public void accept(EntityActionContext context) {

        if (!(context.entity() instanceof ServerPlayer serverPlayer)) {
            return;
        }

        MinecraftServer server = serverPlayer.server;
        ServerAdvancementLoader advancementLoader = server.getAdvancementLoader();

        if (selection == AdvancementCommands.Selection.EVERYTHING) {
            AdvancementUtil.processAdvancements(advancementLoader.getAdvancements(), AdvancementCommands.Operation.REVOKE, serverPlayer);
        }

        else if (advancementId.isPresent()) {

            Identifier actualAdvancementId = advancementId.get();
            AdvancementHolder advancementEntry = advancementLoader.get(actualAdvancementId);

            if (advancementEntry == null) {
//                Apoli.LOGGER.warn("Unknown advancement \"{}\" referenced in an entity action that uses the `revoke_advancement` type!", actualAdvancementId);
                return;
            }

            else if (criteria.isEmpty()) {
                AdvancementUtil.processAdvancements(AdvancementUtil.selectEntries(advancementLoader.getManager(), advancementEntry, selection), AdvancementCommands.Operation.REVOKE, serverPlayer);
            }

            else {
                AdvancementUtil.processCriteria(advancementEntry, criteria, AdvancementCommands.Operation.REVOKE, serverPlayer);
            }

        }

    }

    @Override
    public @NotNull ActionConfiguration<?> getConfig() {
        return EntityActionTypes.REVOKE_ADVANCEMENT;
    }

}
