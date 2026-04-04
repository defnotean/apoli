package io.github.apace100.apoli.access;

import io.github.apace100.apoli.power.type.ModifyGrindstonePowerType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface PowerModifiedGrindstone {

    List<ModifyGrindstonePowerType> apoli$getAppliedPowers();

    Player apoli$getPlayer();

    @Nullable
    BlockPos apoli$getPos();

}
