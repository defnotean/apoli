package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.condition.EntityCondition;
import io.github.ladysnake.pal.AbilitySource;
import io.github.ladysnake.pal.Pal;
import io.github.ladysnake.pal.PlayerAbility;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;

import java.util.Objects;
import java.util.Optional;

//  TODO: Implement this as a standalone power type -eggohito
public abstract class PlayerAbilityPowerType extends PowerType {

    protected final PlayerAbility ability;
    protected final int priority;

    private AbilitySource source;
    private boolean shouldRefresh;

    public PlayerAbilityPowerType(PlayerAbility playerAbility, int priority, Optional<EntityCondition> condition) {
        super(condition);
        this.ability = playerAbility;
        this.priority = priority;
    }

    @Override
    public boolean shouldTick() {
        return getHolder() instanceof Player;
    }

    @Override
    public boolean shouldTickWhenInactive() {
        return this.shouldTick();
    }

    @Override
    public void onInit() {
        this.source = Pal.getAbilitySource(getPower().getId(), priority);
        this.shouldRefresh = false;
    }

    @Override
    public Tag toTag() {

        CompoundTag rootNbt = new CompoundTag();
        rootNbt.putBoolean("ShouldRefresh", shouldRefresh);

        return rootNbt;

    }

    @Override
    public void fromTag(Tag tag) {
        if (tag instanceof CompoundTag rootNbt) {
            this.shouldRefresh = rootNbt.getBoolean("ShouldRefresh");
        }
    }

    @Override
    public void serverTick() {

        if (!(getHolder() instanceof Player player)) {
            return;
        }

        if (shouldRefresh) {
            this.ability.getTracker(player).refresh(true);
            this.shouldRefresh = false;
        }

        else {

            boolean active = this.isActive();
            boolean hasAbility = this.hasAbility();

            if (active && !hasAbility) {
                this.grantAbility();
            }

            else if (!active && hasAbility) {
                this.revokeAbility();
            }

        }

    }

    @Override
    public void onAdded() {
        if (getHolder() instanceof ServerPlayer serverPlayer && Apoli.LEGACY_POWER_SOURCE.grants(serverPlayer, ability)) {
            Apoli.LEGACY_POWER_SOURCE.revokeFrom(serverPlayer, ability);
        }
    }

    @Override
    public void onRemoved() {
        //  Indicate that the ability should be refreshed upon the entity being removed from a world
        this.shouldRefresh = true;
    }

    @Override
    public void onGained() {
        if (!getHolder().level().isClientSide && this.isActive()) {
            grantAbility();
        }
    }

    @Override
    public void onLost() {
        if (!getHolder().level().isClientSide) {
            revokeAbility();
        }
    }

    protected AbilitySource getSource() {
        return Objects.requireNonNull(source, "The source of ability \"" + ability.getId() + "\" wasn't initialized yet!");
    }

    public boolean hasAbility() {
        return getHolder() instanceof Player playerEntity && getSource().grants(playerEntity, ability);
    }

    public void grantAbility() {
        if (getHolder() instanceof Player playerEntity) {
            getSource().grantTo(playerEntity, ability);
        }
    }

    public void revokeAbility() {
        if (getHolder() instanceof Player playerEntity) {
            getSource().revokeFrom(playerEntity, ability);
        }
    }

}
