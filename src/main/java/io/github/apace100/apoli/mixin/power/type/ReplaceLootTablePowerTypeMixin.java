package io.github.apace100.apoli.mixin.power.type;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.datafixers.util.Either;
import io.github.apace100.apoli.access.KeyableLootTable;
import io.github.apace100.apoli.access.LootContextTypeHolder;
import io.github.apace100.apoli.access.ReplacingLootContext;
import io.github.apace100.apoli.power.type.Prioritized;
import io.github.apace100.apoli.power.type.ReplaceLootTablePowerType;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.*;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.world.level.storage.loot.entries.NestedLootTable;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.ReloadableServerRegistries;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.core.registries.Registries;

public abstract class ReplaceLootTablePowerTypeMixin {

	@Mixin(ReloadableServerRegistries.Holder.class)
	public static abstract class Replacer {

		@Inject(method = "<init>", at = @At("TAIL"))
		private void setupLootTables(HolderLookup.Provider registryManager, CallbackInfo ci) {
			registryManager.lookupOrThrow(Registries.LOOT_TABLE).listElements().forEach(reference -> {

				ResourceKey<LootTable> key = reference.key();

				if (reference.value() instanceof KeyableLootTable keyable) {
					keyable.apoli$setup(key, (ReloadableServerRegistries.Holder) (Object) this);
				}

			});
		}

		@ModifyReturnValue(method = "getLootTable", at = @At("RETURN"))
		private LootTable getReplacedOrNormalTable(LootTable original, ResourceKey<LootTable> key) {

			if (key.equals(ReplaceLootTablePowerType.REPLACED_TABLE_KEY)) {
				return ReplaceLootTablePowerType.peek();
			}

			else {
				return original;
			}

		}

	}

	@Mixin(NestedLootTable.class)
	public static abstract class NestedReplacer {

		@SuppressWarnings("unchecked")
		@WrapOperation(method = "createItemStack", at = @At(value = "INVOKE", target = "Lcom/mojang/datafixers/util/Either;map(Ljava/util/function/Function;Ljava/util/function/Function;)Ljava/lang/Object;"))
		private <T, L extends ResourceKey<LootTable>, R extends LootTable> T replaceGetter(Either<L, R> either, Function<? super L, ? extends T> leftFunction, Function<? super R, ? extends T> rightFunction, Operation<T> original, Consumer<ItemStack> stackConsumer, LootContext lootContext) {

			ReloadableServerRegistries.Holder lookup = lootContext.getLevel().getServer().reloadableRegistries();
			Function<? super L, ? extends T> newGetter = l -> (T) lookup.getLootTable(l);

			return original.call(either, newGetter, rightFunction);

		}

	}

	@Mixin(LootTable.class)
	public static abstract class LootTableCache implements KeyableLootTable {

		@Unique
		private ResourceKey<LootTable> apoli$key;

		@Unique
		private ReloadableServerRegistries.Holder apoli$lookup;

		@Override
		public ResourceKey<LootTable> apoli$getKey() {
			return apoli$key;
		}

		@Override
		public void apoli$setup(ResourceKey<LootTable> lootTableKey, ReloadableServerRegistries.Holder lookup) {
			this.apoli$key = lootTableKey;
			this.apoli$lookup = lookup;
		}

		@Inject(method = "getRandomItemsRaw(Lnet/minecraft/world/level/storage/loot/LootContext;Ljava/util/function/Consumer;)V", at = @At("HEAD"), cancellable = true)
		private void replaceTable(LootContext context, Consumer<ItemStack> lootConsumer, CallbackInfo ci) {

			if (!(context instanceof ReplacingLootContext replacingContext)) {
				return;
			}

			ContextKeySet contextType = replacingContext.apoli$getType();
			ResourceKey<LootTable> key = this.apoli$getKey();

			if (key == null || replacingContext.apoli$isReplaced(key)) {
				return;
			}

			Entity thisEntity = context.getParameter(LootContextParams.THIS_ENTITY);
			Entity holder = thisEntity;

			if (contextType == LootContextParamSets.FISHING) {

				if (thisEntity instanceof FishingHook bobber) {
					holder = bobber.getOwner();
				}

			}

			else if (contextType == LootContextParamSets.ENTITY) {

				if (context.getOptionalParameter(LootContextParams.ATTACKING_ENTITY) != null) {
					holder = context.getParameter(LootContextParams.ATTACKING_ENTITY);
				}

			}

			else if (contextType == LootContextParamSets.PIGLIN_BARTER) {

				if (thisEntity instanceof Piglin piglin) {
					holder = piglin.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_PLAYER).orElse(null);
				}

			}

			ReplaceLootTablePowerType.push((LootTable) (Object) this);
			Prioritized.CallInstance<ReplaceLootTablePowerType> types = new Prioritized.CallInstance<>();

			Optional<LootTable> replacementTable = Optional.empty();
			types.add(holder, ReplaceLootTablePowerType.class, type -> type.hasReplacement(key) && type.doesApply(context));

			for (int priority = types.getMaxPriority(); priority >= types.getMinPriority(); priority--) {

				for (var type : types.getPowerTypes(priority)) {

					replacementTable = type.getReplacement(key)
						.map(this.apoli$lookup::getLootTable)
						.filter(Predicate.not(LootTable.EMPTY::equals));

				}

			}

			if (replacementTable.isEmpty()) {
				return;
			}

			LootTable table = replacementTable.get();
			replacingContext.apoli$setReplaced(key);

			table.getRandomItemsRaw(context, lootConsumer);
			ci.cancel();

		}

		@WrapMethod(method = "getRandomItemsRaw(Lnet/minecraft/world/level/storage/loot/LootContext;Ljava/util/function/Consumer;)V")
		private void wrapGenerateForReplacing(LootContext context, Consumer<ItemStack> lootConsumer, Operation<Void> original) {

			try {
				original.call(context, lootConsumer);
			}

			finally {
				ReplaceLootTablePowerType.clear();
			}

		}

		@Inject(method = "getRandomItemsRaw(Lnet/minecraft/world/level/storage/loot/LootContext;Ljava/util/function/Consumer;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/loot/LootContext;pushVisitedElement(Lnet/minecraft/world/level/storage/loot/LootContext$VisitedEntry;)Z"))
		private void popReplaced(LootContext context, Consumer<ItemStack> lootConsumer, CallbackInfo ci) {
			ReplaceLootTablePowerType.pop();
		}

		@Inject(method = "getRandomItemsRaw(Lnet/minecraft/world/level/storage/loot/LootContext;Ljava/util/function/Consumer;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/loot/LootContext;popVisitedElement(Lnet/minecraft/world/level/storage/loot/LootContext$VisitedEntry;)V"))
		private void restoreReplaced(LootContext context, Consumer<ItemStack> lootConsumer, CallbackInfo ci) {
			ReplaceLootTablePowerType.restore();
		}

	}

	@Mixin(LootContext.class)
	public static abstract class LootContextCache implements ReplacingLootContext {

		@Shadow
		@Final
		private LootParams params;

		@Unique
		private final Set<ResourceKey<LootTable>> apoli$replacedTables = new ObjectOpenHashSet<>();

		@Override
		public ContextKeySet apoli$getType() {
			return ((LootContextTypeHolder) this.params).apoli$getType();
		}

		@Override
		public boolean apoli$isReplaced(ResourceKey<LootTable> key) {
			return apoli$replacedTables.contains(key);
		}

		@Override
		public void apoli$setReplaced(ResourceKey<LootTable> key) {
			this.apoli$replacedTables.add(key);
		}

	}

	@Mixin(LootParams.class)
	public static abstract class LootContextParametersCache implements LootContextTypeHolder {

		@Unique
		private ContextKeySet apoli$contextType;

		@Override
		public ContextKeySet apoli$getType() {
			return Objects.requireNonNull(this.apoli$contextType, "Loot context parameters are not initialized properly!");
		}

		@Override
		public void apoli$setType(ContextKeySet type) {
			this.apoli$contextType = type;
		}

	}

	@Mixin(LootParams.Builder.class)
	public static abstract class LootContextParametersCacheInit {

		@ModifyReturnValue(method = "build", at = @At("RETURN"))
		private LootParams cacheType(LootParams original, ContextKeySet type) {

			((LootContextTypeHolder) original).apoli$setType(type);

			return original;

		}

	}

}
