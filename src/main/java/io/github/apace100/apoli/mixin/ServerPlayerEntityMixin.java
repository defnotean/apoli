package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Cancellable;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Either;
import io.github.apace100.apoli.access.CustomToastViewer;
import io.github.apace100.apoli.access.EndRespawningEntity;
import io.github.apace100.apoli.access.PowerCraftingObject;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.data.CustomToastData;
import io.github.apace100.apoli.networking.packet.s2c.ShowToastS2CPacket;
import io.github.apace100.apoli.power.type.ActionOnItemUsePowerType;
import io.github.apace100.apoli.power.type.KeepInventoryPowerType;
import io.github.apace100.apoli.power.type.ModifyPlayerSpawnPowerType;
import io.github.apace100.apoli.power.type.PreventSleepPowerType;
import io.github.apace100.apoli.util.InventoryUtil;
import io.github.apace100.apoli.util.PriorityPhase;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.ServerRecipeBook;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import com.mojang.datafixers.util.Pair;
import net.minecraft.util.Unit;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerEntityMixin extends Player implements ContainerListener, EndRespawningEntity, CustomToastViewer {

    @Shadow
    @Final
    private MinecraftServer server;

    @Shadow
    public ServerGamePacketListenerImpl connection;

    @Shadow
    public abstract void sendSystemMessage(Component message, boolean actionBar);

    @Shadow
    public abstract ServerPlayer.RespawnConfig getRespawnConfig();

    // MC 26.1: findRespawnPosition removed. The respawn system now uses RespawnConfig.
    // findRespawnAndUseSpawnBlock is the new equivalent but has a different signature.

    private ServerPlayerEntityMixin(Level world, GameProfile gameProfile) {
        super(world, gameProfile);
    }

    @WrapOperation(method = "startSleepInBed", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;setRespawnPosition(Lnet/minecraft/server/level/ServerPlayer$RespawnConfig;Z)V"))
    private void apoli$preventSleep(ServerPlayer serverPlayer, ServerPlayer.RespawnConfig config, boolean sendMessage, Operation<Void> original, @Cancellable CallbackInfoReturnable<Either<Player.BedSleepingProblem, Unit>> cir) {

        // Extract position from RespawnConfig for condition checks
        BlockPos pos = this.blockPosition();

        List<PreventSleepPowerType> preventSleepPowers = PowerHolderComponent.getPowerTypes(this, PreventSleepPowerType.class)
            .stream()
            .filter(type -> type.doesPrevent(this.level(), pos))
            .sorted(Comparator.comparing(PreventSleepPowerType::getPriority))
            .toList();

        if (preventSleepPowers.isEmpty()) {
            original.call(serverPlayer, config, sendMessage);
        }

        else {

            if (preventSleepPowers.stream().allMatch(PreventSleepPowerType::doesAllowSpawnPoint)) {
                original.call(serverPlayer, config, sendMessage);
            }

            cir.setReturnValue(Either.left(Player.BedSleepingProblem.OTHER_PROBLEM));
            this.sendSystemMessage(preventSleepPowers.getLast().getMessage(), true);

        }

    }

    // TODO: MC 26.1 replaced getRespawnDimension/getRespawnPosition/isRespawnForced with RespawnConfig.
    // ModifyPlayerSpawnPowerType needs to be reimplemented to work with the new getRespawnConfig() API.
    // The RespawnConfig object now encapsulates dimension, position, angle, and forced flag together.

    // MC 26.1: getRespawnTarget and findRespawnPosition replaced by findRespawnPositionAndUseSpawnBlock.
    // The respawn system now uses RespawnConfig objects. The retry logic for obstructed spawn points
    // needs to be adapted to the new system in a future update.
    // TODO: Reimplement spawn point retry logic for MC 26.1 RespawnConfig system.

    @Inject(method = "restoreFrom", at = @At("TAIL"))
    private void copyInventoryWhenKeeping(ServerPlayer oldPlayer, boolean alive, CallbackInfo ci) {
        if(PowerHolderComponent.hasPowerType(oldPlayer, KeepInventoryPowerType.class)) {
            this.getInventory().replaceWith(oldPlayer.getInventory());
        }
    }

    // TODO: MC 26.1 - These helper methods need reimplementation using RespawnConfig API.
    // apoli$hasObstructedOriginalSpawnPoint and apoli$findPowerSpawnPoint relied on
    // spawnPointDimension/spawnPointPosition/spawnForced fields which are now inside RespawnConfig.

    @Inject(method = "drop", at = @At("HEAD"))
    private void cacheItemStackBeforeDropping(boolean entireStack, CallbackInfoReturnable<Boolean> cir, @Share("prevSelectedStack") LocalRef<ItemStack> prevSelectedStackLocRef) {
        prevSelectedStackLocRef.set(this.getInventory().getSelectedItem().copy());
    }

    @ModifyArg(method = "drop", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayer;drop(Lnet/minecraft/world/item/ItemStack;ZZ)Lnet/minecraft/world/entity/ItemEntity;"))
    private ItemStack checkItemUsageStopping(ItemStack original, @Share("prevSelectedStack") LocalRef<ItemStack> prevSelectedStackLocRef) {

        ItemStack prevSelectedStack = prevSelectedStackLocRef.get();
        if (!this.isUsingItem() || ItemStack.matches(prevSelectedStack, this.getInventory().getSelectedItem())) {
            return original;
        }

        SlotAccess newSelectedStackRef = InventoryUtil.createStackReference(original);
        ActionOnItemUsePowerType.executeActions(this, newSelectedStackRef, prevSelectedStack, ActionOnItemUsePowerType.TriggerType.STOP, PriorityPhase.ALL);

        return newSelectedStackRef.get();

    }

    @Unique
    private boolean apoli$isEndRespawning;

    @Override
    public void apoli$setEndRespawning(boolean endSpawn) {
        this.apoli$isEndRespawning = endSpawn;
    }

    @Override
    public boolean apoli$isEndRespawning() {
        return this.apoli$isEndRespawning;
    }

    @Override
    public boolean apoli$hasRealRespawnPoint() {
        // TODO: MC 26.1 - Use RespawnConfig to determine real respawn point
        return this.getRespawnConfig() != null;
    }

    @Override
    public void apoli$showToast(CustomToastData toastData) {
        ServerPlayNetworking.send((ServerPlayer) (Object) this, new ShowToastS2CPacket(toastData));
    }

    @ModifyExpressionValue(method = "<init>", at = @At(value = "NEW", target = "()Lnet/minecraft/stats/ServerRecipeBook;"))
    private ServerRecipeBook apoli$cachePlayerToRecipeBook(ServerRecipeBook original) {

        if (original instanceof PowerCraftingObject pco) {
            pco.apoli$setPlayer(this);
        }

        return original;

    }

}
