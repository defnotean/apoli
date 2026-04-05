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

    // PAL (PlayerAbilityLib) is compiled against intermediary class names which don't exist
    // in deobfuscated MC 26.1. We use reflection to bridge the gap at runtime, since at
    // runtime the classes are the same objects regardless of name.
    @SuppressWarnings("all")
    private static AbilitySource palGetAbilitySource(net.minecraft.resources.Identifier id, int priority) {
        try {
            java.lang.reflect.Method m = Pal.class.getMethod("getAbilitySource", Object.class, int.class);
            return (AbilitySource) m.invoke(null, id, priority);
        } catch (Exception e) {
            // Fallback: try the two-arg Identifier overload
            try {
                for (java.lang.reflect.Method m : Pal.class.getMethods()) {
                    if (m.getName().equals("getAbilitySource") && m.getParameterCount() == 2
                        && m.getParameterTypes()[1] == int.class) {
                        return (AbilitySource) m.invoke(null, id, priority);
                    }
                }
            } catch (Exception e2) { /* fall through */ }
            throw new RuntimeException("Failed to call Pal.getAbilitySource", e);
        }
    }

    @SuppressWarnings("all")
    private static void palGrantTo(AbilitySource source, Player player, PlayerAbility ability) {
        try {
            for (java.lang.reflect.Method m : AbilitySource.class.getMethods()) {
                if (m.getName().equals("grantTo") && m.getParameterCount() == 2) {
                    m.invoke(source, player, ability);
                    return;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to call AbilitySource.grantTo", e);
        }
    }

    @SuppressWarnings("all")
    private static void palRevokeFrom(AbilitySource source, Player player, PlayerAbility ability) {
        try {
            for (java.lang.reflect.Method m : AbilitySource.class.getMethods()) {
                if (m.getName().equals("revokeFrom") && m.getParameterCount() == 2) {
                    m.invoke(source, player, ability);
                    return;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to call AbilitySource.revokeFrom", e);
        }
    }

    @SuppressWarnings("all")
    private static boolean palGrants(AbilitySource source, Player player, PlayerAbility ability) {
        try {
            for (java.lang.reflect.Method m : AbilitySource.class.getMethods()) {
                if (m.getName().equals("grants") && m.getParameterCount() == 2) {
                    return (boolean) m.invoke(source, player, ability);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to call AbilitySource.grants", e);
        }
        return false;
    }

    @SuppressWarnings("all")
    private static void palRefreshTracker(PlayerAbility ability, Player player) {
        try {
            for (java.lang.reflect.Method m : PlayerAbility.class.getMethods()) {
                if (m.getName().equals("getTracker") && m.getParameterCount() == 1) {
                    Object tracker = m.invoke(ability, player);
                    tracker.getClass().getMethod("refresh", boolean.class).invoke(tracker, true);
                    return;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to call PlayerAbility.getTracker", e);
        }
    }

    @Override
    public void onInit() {
        this.source = palGetAbilitySource(getPower().getId(), priority);
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
            this.shouldRefresh = rootNbt.getBooleanOr("ShouldRefresh", false);
        }
    }

    @Override
    public void serverTick() {

        if (!(getHolder() instanceof Player player)) {
            return;
        }

        if (shouldRefresh) {
            palRefreshTracker(this.ability, player);
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
        // PAL legacy power source is disabled until PAL updates for MC 26.1
        // When re-enabled: if (getHolder() instanceof ServerPlayer serverPlayer && Apoli.LEGACY_POWER_SOURCE.grants(serverPlayer, ability)) {
        //     Apoli.LEGACY_POWER_SOURCE.revokeFrom(serverPlayer, ability);
        // }
    }

    @Override
    public void onRemoved() {
        //  Indicate that the ability should be refreshed upon the entity being removed from a world
        this.shouldRefresh = true;
    }

    @Override
    public void onGained() {
        if (!getHolder().level().isClientSide() && this.isActive()) {
            grantAbility();
        }
    }

    @Override
    public void onLost() {
        if (!getHolder().level().isClientSide()) {
            revokeAbility();
        }
    }

    protected AbilitySource getSource() {
        return Objects.requireNonNull(source, "The source of ability \"" + ability + "\" wasn't initialized yet!");
    }

    public boolean hasAbility() {
        return getHolder() instanceof Player playerEntity && palGrants(getSource(), playerEntity, ability);
    }

    public void grantAbility() {
        if (getHolder() instanceof Player playerEntity) {
            palGrantTo(getSource(), playerEntity, ability);
        }
    }

    public void revokeAbility() {
        if (getHolder() instanceof Player playerEntity) {
            palRevokeFrom(getSource(), playerEntity, ability);
        }
    }

}
