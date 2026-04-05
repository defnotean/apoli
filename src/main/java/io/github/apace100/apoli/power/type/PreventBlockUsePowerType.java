package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.action.BlockAction;
import io.github.apace100.apoli.action.EntityAction;
import io.github.apace100.apoli.action.ItemAction;
import io.github.apace100.apoli.condition.BlockCondition;
import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.condition.ItemCondition;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.power.PowerConfiguration;
import io.github.apace100.apoli.util.BlockUsagePhase;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;
import java.util.Optional;

public class PreventBlockUsePowerType extends ActiveInteractionPowerType {

    public static final TypedDataObjectFactory<PreventBlockUsePowerType> DATA_FACTORY = ActiveInteractionPowerType.createConditionedDataFactory(
        new SerializableData()
            .add("entity_action", EntityAction.DATA_TYPE.optional(), Optional.empty())
            .add("block_action", BlockAction.DATA_TYPE.optional(), Optional.empty())
            .add("block_condition", BlockCondition.DATA_TYPE.optional(), Optional.empty())
            .add("use_phases", ApoliDataTypes.BLOCK_USAGE_PHASE_SET, EnumSet.allOf(BlockUsagePhase.class))
            .add("directions", SerializableDataTypes.DIRECTION_SET, EnumSet.allOf(Direction.class)),
        (data, heldItemAction, heldItemCondition, resultItemAction, resultStack, hands, actionResult, priority, condition) -> new PreventBlockUsePowerType(
            data.get("entity_action"),
            data.get("block_action"),
            data.get("block_condition"),
            data.get("use_phases"),
            data.get("directions"),
            heldItemAction,
            heldItemCondition,
            resultItemAction,
            resultStack,
            hands,
            priority,
            condition
        ),
        (powerType, serializableData) -> serializableData.instance()
            .set("entity_action", powerType.entityAction)
            .set("block_action", powerType.blockAction)
            .set("block_condition", powerType.blockCondition)
            .set("use_phases", powerType.usePhases)
            .set("directions", powerType.directions)
    );

    private final Optional<EntityAction> entityAction;
    private final Optional<BlockAction> blockAction;

    private final Optional<BlockCondition> blockCondition;

    private final EnumSet<BlockUsagePhase> usePhases;
    private final EnumSet<Direction> directions;

    public PreventBlockUsePowerType(Optional<EntityAction> entityAction, Optional<BlockAction> blockAction, Optional<BlockCondition> blockCondition, EnumSet<BlockUsagePhase> usePhases, EnumSet<Direction> directions, Optional<ItemAction> heldItemAction, Optional<ItemCondition> heldItemCondition, Optional<ItemAction> resultItemAction, Optional<ItemStack> resultStack, EnumSet<InteractionHand> hands, int priority, Optional<EntityCondition> condition) {
        super(heldItemAction, heldItemCondition, resultItemAction, resultStack, hands, InteractionResult.FAIL, priority, condition);
        this.blockCondition = blockCondition;
        this.entityAction = entityAction;
        this.blockAction = blockAction;
        this.directions = directions;
        this.usePhases = usePhases;
    }

    @Override
    public @NotNull PowerConfiguration<?> getConfig() {
        return PowerTypes.PREVENT_BLOCK_USE;
    }

    public void executeActions(BlockHitResult hitResult, InteractionHand hand) {

        LivingEntity holder = getHolder();

        blockAction.ifPresent(action -> action.execute(holder.level(), hitResult.blockPosition(), Optional.of(hitResult.getDirection())));
        entityAction.ifPresent(action -> action.execute(holder));

        if (holder instanceof Player player) {
            this.performActorItemStuff(player, hand);
        }

    }

    public boolean doesPrevent(BlockUsagePhase usePhase, BlockHitResult hitResult, ItemStack heldStack, InteractionHand hand) {
        return usePhases.contains(usePhase)
            && directions.contains(hitResult.getDirection())
            && super.shouldExecute(hand, heldStack)
            && blockCondition.map(condition -> condition.test(getHolder().level(), hitResult.blockPosition())).orElse(true);
    }

    public static boolean doesPrevent(Entity holder, BlockUsagePhase usePhase, BlockHitResult hitResult, ItemStack heldStack, InteractionHand hand) {

        CallInstance<ActiveInteractionPowerType> aipci = new CallInstance<>();
        aipci.add(holder, PreventBlockUsePowerType.class, p -> p.doesPrevent(usePhase, hitResult, heldStack, hand));

        for (int i = aipci.getMaxPriority(); i >= aipci.getMinPriority(); i--) {
            aipci.forEach(i, p -> ((PreventBlockUsePowerType) p).executeActions(hitResult, hand));
        }

        return !aipci.isEmpty();

    }

}
