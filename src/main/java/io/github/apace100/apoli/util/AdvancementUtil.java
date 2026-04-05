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

    private static boolean includesParents(AdvancementCommands.Mode selection) {
        return selection == AdvancementCommands.Mode.THROUGH
            || selection == AdvancementCommands.Mode.UNTIL
            || selection == AdvancementCommands.Mode.EVERYTHING;
    }

    private static boolean includesChildren(AdvancementCommands.Mode selection) {
        return selection == AdvancementCommands.Mode.THROUGH
            || selection == AdvancementCommands.Mode.FROM
            || selection == AdvancementCommands.Mode.EVERYTHING;
    }

    public static List<AdvancementHolder> selectEntries(AdvancementTree advancementManager, AdvancementHolder advancementEntry, AdvancementCommands.Mode selection) {

        AdvancementNode placedAdvancement = advancementManager.get(advancementEntry);
        if (placedAdvancement == null) {
            return List.of(advancementEntry);
        }

        List<AdvancementHolder> advancementEntries = new ArrayList<>();
        if (includesParents(selection)) {

            for (AdvancementNode parent = placedAdvancement.parent(); parent != null; parent = parent.parent()) {
                advancementEntries.add(parent.holder());
            }

        }

        advancementEntries.add(advancementEntry);
        if (includesChildren(selection)) {
            AdvancementCommandAccessor.callAddChildrenRecursivelyToList(placedAdvancement, advancementEntries);
        }

        return advancementEntries;

    }

    public static void processCriteria(AdvancementHolder advancementEntry, Collection<String> criteria, AdvancementCommands.Action operation, ServerPlayer serverPlayerEntity) {
        for (String criterion : criteria.stream().filter(c -> advancementEntry.value().criteria().containsKey(c)).toList()) {
            if (operation == AdvancementCommands.Action.GRANT) {
                serverPlayerEntity.getAdvancements().award(advancementEntry, criterion);
            } else {
                serverPlayerEntity.getAdvancements().revoke(advancementEntry, criterion);
            }
        }
    }

    public static void processAdvancements(Collection<AdvancementHolder> advancementEntries, AdvancementCommands.Action operation, ServerPlayer serverPlayerEntity) {
        operation.perform(serverPlayerEntity, advancementEntries, false);
    }

}
