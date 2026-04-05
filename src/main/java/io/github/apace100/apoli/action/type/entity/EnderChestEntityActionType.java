package io.github.apace100.apoli.action.type.entity;

import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.context.EntityActionContext;
import io.github.apace100.apoli.action.type.EntityActionType;
import io.github.apace100.apoli.action.type.EntityActionTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.MenuConstructor;
import net.minecraft.stats.Stats;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class EnderChestEntityActionType extends EntityActionType {

    @Override
    public void accept(EntityActionContext context) {

        if (context.entity() instanceof Player player) {

            MenuConstructor handlerFactory = (syncId, playerInventory, _player) -> ChestMenu.threeRows(syncId, playerInventory, player.getEnderChestInventory());
            player.openMenu(new SimpleMenuProvider(handlerFactory, Component.translatable("container.enderchest")));

            player.awardStat(Stats.OPEN_ENDERCHEST);

        }

    }

    @Override
    public @NotNull ActionConfiguration<?> getConfig() {
        return EntityActionTypes.ENDER_CHEST;
    }

}
