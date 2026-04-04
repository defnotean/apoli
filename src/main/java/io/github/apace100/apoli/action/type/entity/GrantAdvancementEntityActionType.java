package io.github.apace100.apoli.action.type.entity;

import io.github.apace100.apoli.Apoli;
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
import net.minecraft.world.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerAdvancementLoader;
import net.minecraft.server.commands.AdvancementCommands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class GrantAdvancementEntityActionType extends EntityActionType {

    public static final TypedDataObjectFactory<GrantAdvancementEntityActionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("advancement", SerializableDataTypes.IDENTIFIER)
            .add("selection", ApoliDataTypes.ADVANCEMENT_SELECTION, AdvancementCommands.Selection.ONLY)
            .add("criterion", SerializableDataTypes.STRING, null)
            .addFunctionedDefault("criteria", SerializableDataTypes.STRINGS, data -> MiscUtil.singletonListOrEmpty(data.get("criterion"))),
        data -> new GrantAdvancementEntityActionType(
            data.get("advancement"),
            data.get("selection"),
            data.get("criteria")
        ),
        (actionType, serializableData) -> serializableData.instance()
            .set("advancement", actionType.advancementId)
            .set("selection", actionType.selection)
            .set("criteria", actionType.criteria)
    );

    private final Identifier advancementId;
    private final AdvancementCommands.Selection selection;

    private final List<String> criteria;

    public GrantAdvancementEntityActionType(Identifier advancementId, AdvancementCommands.Selection selection, List<String> criteria) {
        this.advancementId = advancementId;
        this.selection = selection;
        this.criteria = criteria;
    }

    @Override
    public void accept(EntityActionContext context) {

        Entity entity = context.entity();
        MinecraftServer server = entity.getServer();

        if (server == null || !(entity instanceof ServerPlayer serverPlayerEntity)) {
            return;
        }

        ServerAdvancementLoader advancementLoader = server.getAdvancementLoader();
        if (selection == AdvancementCommands.Selection.EVERYTHING) {
            AdvancementUtil.processAdvancements(advancementLoader.getAdvancements(), AdvancementCommands.Operation.GRANT, serverPlayerEntity);
        }

        else if (advancementId != null) {

            AdvancementHolder advancementEntry = advancementLoader.get(advancementId);
            if (advancementEntry == null) {
                Apoli.LOGGER.warn("Unknown advancement (\"" + advancementId + "\") referenced in `grant_advancement` entity action type!");
            }

            else if (criteria.isEmpty()) {
                AdvancementUtil.processAdvancements(AdvancementUtil.selectEntries(server.getAdvancementLoader().getManager(), advancementEntry, selection), AdvancementCommands.Operation.GRANT, serverPlayerEntity);
            }

            else {
                AdvancementUtil.processCriteria(advancementEntry, criteria, AdvancementCommands.Operation.GRANT, serverPlayerEntity);
            }

        }

    }

    @Override
    public @NotNull ActionConfiguration<?> getConfig() {
        return EntityActionTypes.GRANT_ADVANCEMENT;
    }

}
