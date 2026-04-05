package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.power.PowerConfiguration;
import io.github.apace100.apoli.util.HudRender;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.LongTag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class CooldownPowerType extends PowerType implements HudRendered {

    public static final TypedDataObjectFactory<CooldownPowerType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("hud_render", HudRender.DATA_TYPE, HudRender.DONT_RENDER)
            .add("cooldown", SerializableDataTypes.INT),
        data -> new CooldownPowerType(
            data.get("cooldown"),
            data.get("hud_render")
        ),
        (powerType, serializableData) -> serializableData.instance()
            .set("hud_render", powerType.hudRender)
            .set("cooldown", powerType.cooldown)
    );

    private final HudRender hudRender;
    private final int cooldown;

    protected long lastUseTime;

    public CooldownPowerType(int cooldown, HudRender hudRender) {
        this(cooldown, hudRender, Optional.empty());
    }

    public CooldownPowerType(int cooldown, HudRender hudRender, Optional<EntityCondition> condition) {
        super(condition);
        this.cooldown = cooldown;
        this.hudRender = hudRender;
    }

    @Override
    public @NotNull PowerConfiguration<?> getConfig() {
        return PowerTypes.COOLDOWN;
    }

    @Override
    public boolean isActive() {
        return canUse() && super.isActive();
    }

    @Override
    public Tag toTag() {
        return LongTag.valueOf(lastUseTime);
    }

    @Override
    public void fromTag(Tag tag) {
        lastUseTime = ((LongTag)tag).longValue();
    }

    @Override
    public HudRender getRenderSettings() {
        return hudRender;
    }

    @Override
    public float getFill() {
        return getProgress();
    }

    @Override
    public boolean shouldRender() {
        return (getHolder().level().getGameTime() - lastUseTime) <= cooldown;
    }

    public boolean canUse() {
        return isInitialized()
            && getHolder().level().getGameTime() >= lastUseTime + cooldown;
    }

    public void use() {

        LivingEntity holder = getHolder();
        Level world = holder.level();

        if (world.isClientSide()) {
            return;
        }

        this.lastUseTime = world.getGameTime();
        PowerHolderComponent.syncPower(holder, getPower());

    }

    public float getProgress() {
        float time = getHolder().level().getGameTime() - lastUseTime;
        return Math.min(1F, Math.max(time / (float) cooldown, 0F));
    }

    public int getRemainingTicks() {
        return (int) Math.max(0, cooldown - (getHolder().level().getGameTime() - lastUseTime));
    }

    public int getCooldown() {
        return cooldown;
    }

    public void modify(int changeInTicks) {
        long currentTime = getHolder().level().getGameTime();
        this.lastUseTime = Math.min(lastUseTime + changeInTicks, currentTime);
    }

    public void setCooldown(int cooldownInTicks) {
        long currentTime = getHolder().level().getGameTime();
        this.lastUseTime = currentTime - Math.min(cooldownInTicks, cooldown);
    }

}
