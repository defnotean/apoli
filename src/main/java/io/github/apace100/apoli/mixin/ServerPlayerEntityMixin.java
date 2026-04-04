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
import net.minecraft.world.entity.Dismounting;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerRecipeBook;
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
    private ResourceKey<Level> spawnPointDimension;

    @Shadow
    private BlockPos spawnPointPosition;

    @Shadow
    @Final
    public MinecraftServer server;

    @Shadow
    public ServerGamePacketListenerImpl connection;

    @Shadow
    public abstract void displayClientMessage(Component message, boolean actionBar);

    @Shadow
    private boolean spawnForced;

    @Shadow
    public abstract void sendSystemMessage(Component message);

    @Shadow
    public abstract boolean shouldDamagePlayer(Player player);

    @Shadow
    private float spawnAngle;

    @Shadow
    private static Optional<ServerPlayer.RespawnPos> findRespawnPosition(ServerLevel world, BlockPos pos, float spawnAngle, boolean spawnForced, boolean alive) {
        throw new AssertionError();
    }

    private ServerPlayerEntityMixin(Level world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }

    @WrapOperation(method = "startSleepInBed", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayer;setSpawnPoint(Lnet/minecraft/core/ResourceKey;Lnet/minecraft/core/BlockPos;FZZ)V"))
    private void apoli$preventSleep(ServerPlayer serverPlayer, ResourceKey<Level> dimension, BlockPos pos, float angle, boolean forced, boolean sendMessage, Operation<Void> original, @Cancellable CallbackInfoReturnable<Either<Player.BedSleepingProblem, Unit>> cir) {

        List<PreventSleepPowerType> preventSleepPowers = PowerHolderComponent.getPowerTypes(this, PreventSleepPowerType.class)
            .stream()
            .filter(type -> type.doesPrevent(this.level(), pos))
            .sorted(Comparator.comparing(PreventSleepPowerType::getPriority))
            .toList();

        if (preventSleepPowers.isEmpty()) {
            original.call(serverPlayer, dimension, pos, angle, forced, sendMessage);
        }

        else {

            if (preventSleepPowers.stream().allMatch(PreventSleepPowerType::doesAllowSpawnPoint)) {
                original.call(serverPlayer, dimension, pos, angle, forced, sendMessage);
            }

            cir.setReturnValue(Either.left(Player.BedSleepingProblem.OTHER_PROBLEM));
            this.displayClientMessage(preventSleepPowers.getLast().getMessage(), true);

        }

    }

    @ModifyReturnValue(method = "getRespawnDimension", at = @At("RETURN"))
    private ResourceKey<Level> apoli$modifySpawnPointDimension(ResourceKey<Level> original) {

        if (!this.apoli$isEndRespawning() && (this.spawnPointPosition == null || this.apoli$hasObstructedOriginalSpawnPoint())) {
            return PowerHolderComponent.getPowerTypes(this, ModifyPlayerSpawnPowerType.class)
                .stream()
                .max(Comparator.comparing(ModifyPlayerSpawnPowerType::getPriority))
                .map(ModifyPlayerSpawnPowerType::getDimensionKey)
                .orElse(original);
        }

        else {
            return original;
        }

    }

    @ModifyReturnValue(method = "getRespawnPosition", at = @At("RETURN"))
    private BlockPos apoli$modifySpawnPointPosition(BlockPos original) {

        if (this.apoli$isEndRespawning() || !PowerHolderComponent.hasPowerType(this, ModifyPlayerSpawnPowerType.class)) {
            return original;
        }

        else if (original == null) {
            return this.apoli$findPowerSpawnPoint();
        }

        else if (this.apoli$hasObstructedOriginalSpawnPoint()) {
            this.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.NO_RESPAWN_BLOCK, 0.0F));
            return this.apoli$findPowerSpawnPoint();
        }

        else {
            return original;
        }

    }

    @ModifyReturnValue(method = "isRespawnForced", at = @At("RETURN"))
    private boolean apoli$modifySpawnForced(boolean original) {
        return original || (!this.apoli$isEndRespawning() && (spawnPointPosition == null || this.apoli$hasObstructedOriginalSpawnPoint()) && PowerHolderComponent.hasPowerType(this, ModifyPlayerSpawnPowerType.class));
    }

	@WrapOperation(method = "getRespawnTarget", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayer;findRespawnPosition(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;FZZ)Ljava/util/Optional;"))
	private Optional<ServerPlayer.RespawnPos> apoli$retryObstructedSpawnPointIfFailed(ServerLevel world, BlockPos pos, float spawnAngle, boolean spawnForced, boolean alive, Operation<Optional<ServerPlayer.RespawnPos>> original) {

	    Optional<ServerPlayer.RespawnPos> originalRespawnPos = original.call(world, pos, spawnAngle, spawnForced, alive);

        if (originalRespawnPos.isEmpty() && PowerHolderComponent.hasPowerType(this, ModifyPlayerSpawnPowerType.class)) {
            return Optional
                .ofNullable(Dismounting.findRespawnPos(this.getType(), world, pos, spawnForced))
                .map(newPos -> ServerPlayer.RespawnPos.fromCurrentPos(newPos, pos));
        }

        else {
            return originalRespawnPos;
        }

	}

    @Inject(method = "restoreFrom", at = @At(value = "FIELD", opcode = Opcodes.GETFIELD, target = "Lnet/minecraft/server/network/ServerPlayer;enchantmentTableSeed:I"))
    private void copyInventoryWhenKeeping(ServerPlayer oldPlayer, boolean alive, CallbackInfo ci) {
        if(PowerHolderComponent.hasPowerType(oldPlayer, KeepInventoryPowerType.class)) {
            this.getInventory().clone(oldPlayer.getInventory());
        }
    }

    @Unique
    private boolean apoli$hasObstructedOriginalSpawnPoint() {
        ServerLevel spawnPointWorld = this.server.getLevel(spawnPointDimension);
        return spawnPointPosition != null
            && spawnPointWorld != null
            && findRespawnPosition(spawnPointWorld, this.spawnPointPosition, this.spawnAngle, this.spawnForced, true).isEmpty();
    }

    @Unique
    private BlockPos apoli$findPowerSpawnPoint() {
        return PowerHolderComponent.getPowerTypes(this, ModifyPlayerSpawnPowerType.class)
            .stream()
            .max(Comparator.comparing(ModifyPlayerSpawnPowerType::getPriority))
            .flatMap(ModifyPlayerSpawnPowerType::getSpawn)
            .map(Pair::getRight)
            .orElse(null);
    }

    @Inject(method = "drop", at = @At("HEAD"))
    private void cacheItemStackBeforeDropping(boolean entireStack, CallbackInfoReturnable<Boolean> cir, @Share("prevSelectedStack") LocalRef<ItemStack> prevSelectedStackLocRef) {
        prevSelectedStackLocRef.set(this.getInventory().getMainHandItem().copy());
    }

    @ModifyArg(method = "drop", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayer;drop(Lnet/minecraft/world/item/ItemStack;ZZ)Lnet/minecraft/world/entity/ItemEntity;"))
    private ItemStack checkItemUsageStopping(ItemStack original, @Share("prevSelectedStack") LocalRef<ItemStack> prevSelectedStackLocRef) {

        ItemStack prevSelectedStack = prevSelectedStackLocRef.get();
        if (!this.isUsingItem() || ItemStack.areEqual(prevSelectedStack, this.getInventory().getMainHandItem())) {
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
        return spawnPointPosition != null && !apoli$hasObstructedOriginalSpawnPoint();
    }

    @Override
    public void apoli$showToast(CustomToastData toastData) {
        ServerPlayNetworking.send((ServerPlayer) (Object) this, new ShowToastS2CPacket(toastData));
    }

    @ModifyExpressionValue(method = "<init>", at = @At(value = "NEW", target = "()Lnet/minecraft/server/network/ServerRecipeBook;"))
    private ServerRecipeBook apoli$cachePlayerToRecipeBook(ServerRecipeBook original) {

        if (original instanceof PowerCraftingObject pco) {
            pco.apoli$setPlayer(this);
        }

        return original;

    }

}
