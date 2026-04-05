package io.github.apace100.apoli.condition.type.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.context.EntityConditionContext;
import io.github.apace100.apoli.condition.type.EntityConditionType;
import io.github.apace100.apoli.condition.type.EntityConditionTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.mixin.ClientAdvancementManagerAccessor;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.client.multiplayer.ClientAdvancements;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class AdvancementEntityConditionType extends EntityConditionType {

    public static final TypedDataObjectFactory<AdvancementEntityConditionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("advancement", SerializableDataTypes.IDENTIFIER),
        data -> new AdvancementEntityConditionType(
            data.get("advancement")
        ),
        (conditionType, serializableData) -> serializableData.instance()
            .set("advancement", conditionType.advancement)
    );

    private final Identifier advancement;

    public AdvancementEntityConditionType(Identifier advancement) {
        this.advancement = advancement;
    }

    @Override
    public boolean test(EntityConditionContext context) {

        if (!(context.entity() instanceof Player player)) {
            return false;
        }

        MinecraftServer server = player.level().getServer();
        if (server != null) {

            AdvancementHolder advancementEntry = server.getAdvancements().get(advancement);
            if (advancementEntry == null) {
                //  TODO: Throw an exception and pass it to the factory instance to be caught instead -eggohito
                Apoli.LOGGER.warn("Advancement \"{}\" did not exist, but was referenced in an \"advancement\" entity condition!", advancement);
                return false;
            }

            else {
                return ((ServerPlayer) player).getAdvancementTracker()
                    .getProgress(advancementEntry)
                    .isDone();
            }

        }

        else if (player instanceof LocalPlayer clientPlayer && clientPlayer.connection != null) {

            ClientAdvancements advancementManager = clientPlayer.connection.getAdvancementHandler();
            AdvancementHolder advancement = advancementManager.get(this.advancement);

            if (advancement == null) {
                //  We don't want to print an error here if the advancement does not exist,
                //  because on the client-side, the advancement could just not have been received from the server
                return false;
            }

            Map<AdvancementHolder, AdvancementProgress> progresses = ((ClientAdvancementManagerAccessor) advancementManager).getAdvancementProgresses();
            AdvancementProgress progress = progresses.get(advancement);

            return progress != null
                && progress.isDone();

        }

        else {
            return false;
        }

    }

    @Override
    public @NotNull ConditionConfiguration<?> getConfig() {
        return EntityConditionTypes.ADVANCEMENT;
    }

}
