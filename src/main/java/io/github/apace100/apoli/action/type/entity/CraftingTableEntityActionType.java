package io.github.apace100.apoli.action.type.entity;

import io.github.apace100.apoli.access.ScreenHandlerUsabilityOverride;
import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.context.EntityActionContext;
import io.github.apace100.apoli.action.type.EntityActionType;
import io.github.apace100.apoli.action.type.EntityActionTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuConstructor;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.stats.Stats;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class CraftingTableEntityActionType extends EntityActionType {

    @Override
    public void accept(EntityActionContext context) {

        if (context.entity() instanceof Player player) {

            MenuConstructor handlerFactory = (syncId, playerInventory, _player) -> {

                CraftingMenu craftingScreenHandler = new CraftingMenu(syncId, playerInventory, ContainerLevelAccess.create(player.level(), player.getBlockPos()));
                ((ScreenHandlerUsabilityOverride) craftingScreenHandler).apoli$canUse(true);

                return craftingScreenHandler;

            };

            player.openMenu(new SimpleMenuProvider(handlerFactory, Component.translatable("container.crafting")));
            player.awardStat(Stats.INTERACT_WITH_CRAFTING_TABLE);

        }

    }

    @Override
    public @NotNull ActionConfiguration<?> getConfig() {
        return EntityActionTypes.CRAFTING_TABLE;
    }

}
