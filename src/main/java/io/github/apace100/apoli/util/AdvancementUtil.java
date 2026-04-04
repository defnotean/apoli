package io.github.apace100.apoli.util;

import io.github.apace100.apoli.mixin.AdvancementCommandAccessor;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementTree;
import net.minecraft.advancements.AdvancementNode;
import net.minecraft.server.commands.AdvancementCommands;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class AdvancementUtil {

    public static List<AdvancementHolder> selectEntries(AdvancementTree advancementManager, AdvancementHolder advancementEntry, AdvancementCommands.Selection selection) {

        AdvancementNode placedAdvancement = advancementManager.get(advancementEntry);
        if (placedAdvancement == null) {
            return List.of(advancementEntry);
        }

        List<AdvancementHolder> advancementEntries = new ArrayList<>();
        if (selection.before) {

            for (AdvancementNode parent = placedAdvancement.getParent(); parent != null; parent = parent.getParent()) {
                advancementEntries.add(parent.getAdvancementEntry());
            }

        }

        advancementEntries.add(advancementEntry);
        if (selection.after) {
            AdvancementCommandAccessor.callAddChildrenRecursivelyToList(placedAdvancement, advancementEntries);
        }

        return advancementEntries;

    }

    public static void processCriteria(AdvancementHolder advancementEntry, Collection<String> criteria, AdvancementCommands.Operation operation, ServerPlayer serverPlayerEntity) {
        for (String criterion : criteria.stream().filter(c -> advancementEntry.value().criteria().containsKey(c)).toList()) {
            operation.processEachCriterion(serverPlayerEntity, advancementEntry, criterion);
        }
    }

    public static void processAdvancements(Collection<AdvancementHolder> advancementEntries, AdvancementCommands.Operation operation, ServerPlayer serverPlayerEntity) {
        for (AdvancementHolder advancementEntry : advancementEntries) {
            operation.processEach(serverPlayerEntity, advancementEntry);
        }
    }

}
