package io.github.apace100.apoli.action.type.block;

import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.context.BlockActionContext;
import io.github.apace100.apoli.action.type.BlockActionType;
import io.github.apace100.apoli.action.type.BlockActionTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.WorldEvents;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class BoneMealBlockActionType extends BlockActionType {

    public static final TypedDataObjectFactory<BoneMealBlockActionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("effects", SerializableDataTypes.BOOLEAN, true)
            .addFunctionedDefault("show_effects", SerializableDataTypes.BOOLEAN, data -> data.getBoolean("effects")),
        data -> new BoneMealBlockActionType(
            data.get("show_effects")
        ),
        (actionType, serializableData) -> serializableData.instance()
            .set("show_effects", actionType.showEffects)
    );

    private final boolean showEffects;

    public BoneMealBlockActionType(boolean showEffects) {
        this.showEffects = showEffects;
    }


    @Override
    public void accept(BlockActionContext context) {

        Level world = context.world();
        BlockPos pos = context.pos();

        Optional<Direction> optDirection = context.direction();
        ItemStack stack = ItemStack.EMPTY;

        if (BoneMealItem.useOnFertilizable(stack, world, pos)) {
            boneMealEvent(world, pos);
        }

        else if (optDirection.isPresent()) {

            Direction direction = optDirection.get();

            BlockState blockState = world.getBlockState(pos);
            BlockPos offsetPos = pos.offset(direction);

            if (blockState.isSideSolidFullSquare(world, pos, direction) && BoneMealItem.useOnGround(stack, world, offsetPos, direction)) {
                boneMealEvent(world, offsetPos);
            }

        }

    }

    @Override
    public @NotNull ActionConfiguration<?> getConfig() {
        return BlockActionTypes.BONE_MEAL;
    }

    private void boneMealEvent(Level world, BlockPos pos) {

        if (showEffects && !world.isClientSide()) {
            world.syncWorldEvent(WorldEvents.BONE_MEAL_USED, pos, 0);
        }

    }

}
