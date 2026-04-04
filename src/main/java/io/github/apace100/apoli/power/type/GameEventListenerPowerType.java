package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.action.BiEntityAction;
import io.github.apace100.apoli.action.BlockAction;
import io.github.apace100.apoli.condition.BiEntityCondition;
import io.github.apace100.apoli.condition.BlockCondition;
import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.power.PowerConfiguration;
import io.github.apace100.apoli.util.HudRender;
import io.github.apace100.apoli.util.MiscUtil;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.core.Holder;
import net.minecraft.tags.TagKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.gameevent.EntityPositionSource;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;
import net.minecraft.world.level.gameevent.listener.EntityGameEventHandler;
import net.minecraft.world.level.gameevent.listener.GameEventListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class GameEventListenerPowerType extends CooldownPowerType implements VibrationSystem {

    public static final TypedDataObjectFactory<GameEventListenerPowerType> DATA_FACTORY = PowerType.createConditionedDataFactory(
        new SerializableData()
            .add("bientity_action", BiEntityAction.DATA_TYPE.optional(), Optional.empty())
            .add("bientity_condition", BiEntityCondition.DATA_TYPE.optional(), Optional.empty())
            .add("block_action", BlockAction.DATA_TYPE.optional(), Optional.empty())
            .add("block_condition", BlockCondition.DATA_TYPE.optional(), Optional.empty())
            .add("event", SerializableDataTypes.GAME_EVENT_ENTRY, null)
            .addFunctionedDefault("events", SerializableDataTypes.GAME_EVENT_ENTRIES, data -> MiscUtil.singletonListOrEmpty(data.get("event")))
            .add("event_tag", SerializableDataTypes.GAME_EVENT_TAG.optional(), Optional.empty())
            .add("trigger_order", SerializableDataType.enumValue(GameEventListener.TriggerOrder.class), GameEventListener.TriggerOrder.UNSPECIFIED)
            .add("hud_render", HudRender.DATA_TYPE, HudRender.DONT_RENDER)
            .add("cooldown", SerializableDataTypes.POSITIVE_INT, 1)
            .add("show_particle", SerializableDataTypes.BOOLEAN, true)
            .add("range", SerializableDataTypes.POSITIVE_INT, 16),
        (data, condition) -> new GameEventListenerPowerType(
            data.get("bientity_action"),
            data.get("bientity_condition"),
            data.get("block_action"),
            data.get("block_condition"),
            data.get("events"),
            data.get("event_tag"),
            data.get("trigger_order"),
            data.get("hud_render"),
            data.get("cooldown"),
            data.get("show_particle"),
            data.get("range"),
            condition
        ),
        (powerType, serializableData) -> serializableData.instance()
            .set("bientity_action", powerType.biEntityAction)
            .set("bientity_condition", powerType.biEntityCondition)
            .set("block_action", powerType.blockAction)
            .set("block_condition", powerType.blockCondition)
            .set("events", powerType.gameEvents)
            .set("event_tag", powerType.gameEventTag)
            .set("trigger_order", powerType.triggerOrder)
            .set("hud_render", powerType.getRenderSettings())
            .set("cooldown", powerType.getCooldown())
            .set("show_particle", powerType.showParticle)
            .set("range", powerType.range)
    );

    private final Optional<BiEntityAction> biEntityAction;
    private final Optional<BlockAction> blockAction;

    private final Optional<BiEntityCondition> biEntityCondition;
    private final Optional<BlockCondition> blockCondition;

    private final List<Holder<GameEvent>> gameEvents;
    private final Optional<TagKey<GameEvent>> gameEventTag;

    private final GameEventListener.TriggerOrder triggerOrder;

    private final ListenerData vibrationListenerData;
    private final Callback vibrationCallback;

    private final boolean showParticle;
    private final int range;

    private EntityGameEventHandler<VibrationListener> gameEventHandler;

    public GameEventListenerPowerType(Optional<BiEntityAction> biEntityAction, Optional<BiEntityCondition> biEntityCondition, Optional<BlockAction> blockAction, Optional<BlockCondition> blockCondition, List<Holder<GameEvent>> gameEvents, Optional<TagKey<GameEvent>> gameEventTag, GameEventListener.TriggerOrder triggerOrder, HudRender hudRender, int cooldownDuration, boolean showParticle, int range, Optional<EntityCondition> condition) {
        super(cooldownDuration, hudRender, condition);

        this.biEntityAction = biEntityAction;
        this.blockAction = blockAction;

        this.biEntityCondition = biEntityCondition;
        this.blockCondition = blockCondition;

        this.gameEvents = gameEvents;
        this.gameEventTag = gameEventTag;

        this.triggerOrder = triggerOrder;

        this.vibrationListenerData = new ListenerData();
        this.vibrationCallback = new Callback();

        this.showParticle = showParticle;
        this.range = range;

        this.setTicking();

    }

    @Override
    public @NotNull PowerConfiguration<?> getConfig() {
        return PowerTypes.GAME_EVENT_LISTENER;
    }

    @Override
    public void onAdded() {

        if (getHolder().level() instanceof ServerLevel serverWorld) {
            getGameEventHandler().onEntitySetPos(serverWorld);
        }

    }

    @Override
    public void onRemoved() {

        if (getHolder().level() instanceof ServerLevel serverWorld) {
            getGameEventHandler().onEntityRemoval(serverWorld);
        }

    }

    @Override
    public void serverTick() {
        Ticker.tick(getHolder().level(), getVibrationListenerData(), getVibrationCallback());
    }

    @Override
    public boolean canUse() {
        return gameEventHandler != null && super.canUse();
    }

    @Override
    public ListenerData getVibrationListenerData() {
        return vibrationListenerData;
    }

    @Override
    public Callback getVibrationCallback() {
        return vibrationCallback;
    }

    public EntityGameEventHandler<VibrationListener> getGameEventHandler() {

        if (gameEventHandler == null) {
            gameEventHandler = new EntityGameEventHandler<>(new Listener());
        }

        return gameEventHandler;

    }

    public boolean shouldShowParticle() {
        return showParticle;
    }

    public class Listener extends VibrationListener {

        public Listener() {
            super(GameEventListenerPowerType.this);
        }

        @Override
        public TriggerOrder getTriggerOrder() {
            return triggerOrder;
        }

    }

    public class Callback implements VibrationSystem.Callback {

        @Override
        public int getRange() {
            return range;
        }

        @Override
        public PositionSource getPositionSource() {
            LivingEntity holder = getHolder();
            return new EntityPositionSource(holder, holder.getEyeHeight(holder.getPose()));
        }

        @Override
        public boolean accepts(ServerLevel world, BlockPos pos, Holder<GameEvent> event, GameEvent.Emitter emitter) {
            return GameEventListenerPowerType.this.canUse()
                && blockCondition.map(condition -> condition.test(world, pos)).orElse(true)
                && biEntityCondition.map(condition -> condition.test(emitter.sourceEntity(), getHolder())).orElse(true);
        }

        @Override
        public void accept(ServerLevel world, BlockPos pos, Holder<GameEvent> event, @Nullable Entity sourceEntity, @Nullable Entity entity, float distance) {

            GameEventListenerPowerType.this.use();

            blockAction.ifPresent(action -> action.execute(world, pos, Optional.empty()));
            biEntityAction.ifPresent(action -> action.execute(sourceEntity, getHolder()));

        }

        @Override
        public TagKey<GameEvent> getTag() {
            return gameEventTag.orElse(VibrationSystem.Callback.super.getTag());
        }

        public boolean containsEvent(Holder<GameEvent> gameEvent) {
            return gameEventTag.map(gameEvent::isIn).orElse(true)
                && (gameEvents.isEmpty() || gameEvents.contains(gameEvent));
        }

    }

    public class ListenerData extends VibrationSystem.ListenerData {

        public boolean shouldShowParticle() {
            return GameEventListenerPowerType.this.shouldShowParticle();
        }

    }

}
