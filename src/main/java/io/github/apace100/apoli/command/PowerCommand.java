package io.github.apace100.apoli.command;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.serialization.JsonOps;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.command.argument.PowerArgumentType;
import io.github.apace100.apoli.command.argument.PowerHolderArgumentType;
import io.github.apace100.apoli.command.argument.suggestion.PowerSuggestionProvider;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.util.JsonTextFormatter;
import io.github.apace100.apoli.util.MiscUtil;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.resources.Identifier;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class PowerCommand {

	public static Identifier POWER_SOURCE = Apoli.identifier("command");

	public static void register(CommandNode<CommandSourceStack> baseNode) {

		//	The main node of the command
		var powerNode = literal("power")
			.requires(source -> source.hasPermissionLevel(2))
			.build();

		//	Add the sub-nodes as children of the main node
		powerNode.addChild(GrantNode.get());
		powerNode.addChild(RevokeNode.get());
		powerNode.addChild(ListNode.get());
		powerNode.addChild(HasNode.get());
		powerNode.addChild(SourcesNode.get());
		powerNode.addChild(RemoveNode.get());
		powerNode.addChild(ClearNode.get());
		powerNode.addChild(DumpNode.get());

		//	Add the main node as a child of the base node
		baseNode.addChild(powerNode);

	}

	public static class GrantNode {

		public static LiteralCommandNode<CommandSourceStack> get() {
			return literal("grant")
				.then(argument("targets", PowerHolderArgumentType.holders())
					.then(argument("power", PowerArgumentType.power())
						.executes(context -> execute(context, false))
						.then(argument("source", IdentifierArgument.identifier())
							.executes(context -> execute(context, true))))).build();
		}

		public static int execute(CommandContext<CommandSourceStack> context, boolean specifiedSource) throws CommandSyntaxException {

			List<LivingEntity> targets = PowerHolderArgumentType.getHolders(context, "targets");
			Power power = PowerArgumentType.getPower(context, "power");

			Identifier source = specifiedSource
				? IdentifierArgument.getIdentifier(context, "source")
				: POWER_SOURCE;

			CommandSourceStack commandSource = context.getDirectEntity();
			List<LivingEntity> processedTargets = targets.stream()
				.filter(e -> PowerHolderComponent.grantPower(e, power, source, true))
				.toList();

			if (processedTargets.isEmpty()) {

				if (targets.size() == 1) {
					commandSource.sendError(Component.translatable("commands.apoli.grant.fail.single", targets.getFirst().getName(), power.getName(), source.toString()));
				}

				else {
					commandSource.sendError(Component.translatable("commands.apoli.grant.fail.multiple", targets.size(), power.getName(), source.toString()));
				}

			}

			else if (specifiedSource) {

				if (processedTargets.size() == 1) {
					commandSource.sendFeedback(() -> Component.translatable("commands.apoli.grant_from_source.success.single", processedTargets.getFirst().getName(), power.getName(), source.toString()), true);
				}

				else {
					commandSource.sendFeedback(() -> Component.translatable("commands.apoli.grant_from_source.success.multiple", processedTargets.size(), power.getName(), source.toString()), true);
				}

			}

			else {

				if (processedTargets.size() == 1) {
					commandSource.sendFeedback(() -> Component.translatable("commands.apoli.grant.success.single", processedTargets.getFirst().getName(), power.getName()), true);
				}

				else {
					commandSource.sendFeedback(() -> Component.translatable("commands.apoli.grant.success.multiple", processedTargets.size(), power.getName()), true);
				}

			}

			return processedTargets.size();

		}

	}

	public static class RevokeNode {

		public static LiteralCommandNode<CommandSourceStack> get() {
			return literal("revoke")
				.then(argument("targets", PowerHolderArgumentType.holders())
					.then(argument("power", PowerArgumentType.power())
						.suggests(PowerSuggestionProvider.powersFromEntities("targets"))
						.executes(context -> executeSingle(context, false))
						.then(argument("source", IdentifierArgument.identifier())
							.executes(context -> executeSingle(context, true))))
					.then(literal("all")
						.then(argument("source", IdentifierArgument.identifier())
							.executes(RevokeNode::executeAll)))).build();
		}

		public static int executeSingle(CommandContext<CommandSourceStack> context, boolean specifiedSource) throws CommandSyntaxException {

			List<LivingEntity> targets = PowerHolderArgumentType.getHolders(context, "targets");
			Power power = PowerArgumentType.getPower(context, "power");

			Identifier source = specifiedSource
				? IdentifierArgument.getIdentifier(context, "source")
				: POWER_SOURCE;

			CommandSourceStack commandSource = context.getDirectEntity();
			List<LivingEntity> processedTargets = targets.stream()
				.filter(target -> PowerHolderComponent.revokePower(target, power, source, true))
				.toList();

			if (processedTargets.isEmpty()) {

				if (targets.size() == 1) {
					commandSource.sendError(Component.translatable("commands.apoli.revoke.fail.single", targets.getFirst().getName(), power.getName(), source.toString()));
				}

				else {
					commandSource.sendError(Component.translatable("commands.apoli.revoke.fail.multiple", power.getName(), source.toString()));
				}

			}

			else if (specifiedSource) {

				if (processedTargets.size() == 1) {
					commandSource.sendFeedback(() -> Component.translatable("commands.apoli.revoke_from_source.success.single", processedTargets.getFirst().getName(), power.getName(), source.toString()), true);
				}

				else {
					commandSource.sendFeedback(() -> Component.translatable("commands.apoli.revoke_from_source.success.multiple", processedTargets.size(), power.getName(), source.toString()), true);
				}

			}

			else {

				if (processedTargets.size() == 1) {
					commandSource.sendFeedback(() -> Component.translatable("commands.apoli.revoke.success.single", processedTargets.getFirst().getName(), power.getName()), true);
				}

				else {
					commandSource.sendFeedback(() -> Component.translatable("commands.apoli.revoke.success.multiple", processedTargets.size(), power.getName()), true);
				}

			}

			return processedTargets.size();

		}

		public static int executeAll(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {

			List<LivingEntity> targets = PowerHolderArgumentType.getHolders(context, "targets");
			Identifier source = IdentifierArgument.getIdentifier(context, "source");

			CommandSourceStack commandSource = context.getDirectEntity();
			List<LivingEntity> processedTargets = new ObjectArrayList<>();

			AtomicInteger revokedPowers = new AtomicInteger();
			for (LivingEntity target : targets) {

				int revokedPowersFromSource = PowerHolderComponent.revokeAllPowersFromSource(target, source, true);
				revokedPowers.accumulateAndGet(revokedPowersFromSource, Integer::sum);

				if (revokedPowersFromSource > 0) {
					processedTargets.add(target);
				}

			}

			if (processedTargets.isEmpty()) {

				if (targets.size() == 1) {
					commandSource.sendError(Component.translatable("commands.apoli.revoke_all.fail.single", targets.getFirst().getName(), source.toString()));
				}

				else {
					commandSource.sendError(Component.stringifiedTranslatable("commands.apoli.revoke_all.fail.multiple", source));
				}

			}

			else {

				if (processedTargets.size() == 1) {
					commandSource.sendFeedback(() -> Component.translatable("commands.apoli.revoke_all.success.single", processedTargets.getFirst().getName(), revokedPowers.get(), source.toString()), true);
				}

				else {
					commandSource.sendFeedback(() -> Component.translatable("commands.apoli.revoke_all.success.multiple", processedTargets.size(), revokedPowers.get(), source.toString()), true);
				}

			}

			return revokedPowers.get();

		}

	}

	public static class ListNode {

		public static LiteralCommandNode<CommandSourceStack> get() {
			return literal("list")
				.executes(context -> execute(context, true, false))
				.then(argument("target", PowerHolderArgumentType.holder())
					.executes(context -> execute(context, false, false))
					.then(argument("subPowers", BoolArgumentType.bool())
						.executes(context -> execute(context, false, BoolArgumentType.getBool(context, "subPowers"))))).build();
		}

		public static int execute(CommandContext<CommandSourceStack> context, boolean self, boolean includeSubPowers) throws CommandSyntaxException {

			CommandSourceStack commandSource = context.getDirectEntity();
			Entity target = self
				? commandSource.getEntityOrThrow()
				: PowerHolderArgumentType.getHolder(context, "target");

			PowerHolderComponent powerComponent = PowerHolderComponent.KEY
				.maybeGet(target)
				.orElseThrow(() -> PowerHolderArgumentType.HOLDER_NOT_FOUND.create(target.getName()));

			List<Component> powersTooltip = new ObjectArrayList<>();
			for (Power power : powerComponent.getPowers(includeSubPowers)) {

				List<Component> sourcesTooltip = powerComponent.getSources(power)
					.stream()
					.map(Component::of)
					.toList();

				Component joinedSourcesTooltip = Component.translatable("commands.apoli.list.sources", ComponentUtils.join(sourcesTooltip, Component.literal(", ")));
				HoverEvent sourceHoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, joinedSourcesTooltip);

				powersTooltip.add(Component
					.literal(power.getId().toString())
					.setStyle(Style.EMPTY.withHoverEvent(sourceHoverEvent)));

			}

			if (powersTooltip.isEmpty()) {
				commandSource.sendError(Component.translatable("commands.apoli.list.fail", target.getName()));
			}

			else {
				commandSource.sendFeedback(() -> Component.translatable("commands.apoli.list.pass", target.getName(), powersTooltip.size(), ComponentUtils.join(powersTooltip, Component.literal(", "))), false);
			}

			return powersTooltip.size();

		}

	}

	public static class HasNode {

		public static LiteralCommandNode<CommandSourceStack> get() {
			return literal("has")
				.then(argument("targets", PowerHolderArgumentType.holders())
					.then(argument("power", PowerArgumentType.power())
						.executes(HasNode::execute))).build();
		}

		public static int execute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {

			List<LivingEntity> targets = PowerHolderArgumentType.getHolders(context, "targets");
			Power power = PowerArgumentType.getPower(context, "power");

			CommandSourceStack commandSource = context.getDirectEntity();
			List<LivingEntity> processedTargets = targets.stream()
				.filter(target -> PowerHolderComponent.KEY.get(target).hasPower(power))
				.toList();

			if (processedTargets.isEmpty()) {

				if (targets.size() == 1) {
					commandSource.sendError(Component.translatable("commands.execute.conditional.fail"));
				}

				else {
					commandSource.sendError(Component.translatable("commands.execute.conditional.fail_count", targets.size()));
				}

			}

			else {

				if (processedTargets.size() == 1) {
					commandSource.sendFeedback(() -> Component.translatable("commands.execute.conditional.pass"), false);
				}

				else {
					commandSource.sendFeedback(() -> Component.translatable("commands.execute.conditional.pass_count", processedTargets.size()), false);
				}

			}

			return processedTargets.size();

		}

	}

	public static class SourcesNode {

		public static LiteralCommandNode<CommandSourceStack> get() {
			return literal("sources")
				.then(argument("target", PowerHolderArgumentType.holder())
					.then(argument("power", PowerArgumentType.power())
						.suggests(PowerSuggestionProvider.powersFromEntity("target"))
						.executes(SourcesNode::execute)
						.then(literal("")))).build();
		}

		public static int execute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {

			Entity target = PowerHolderArgumentType.getHolder(context, "target");
			Power power = PowerArgumentType.getPower(context, "power");

			CommandSourceStack commandSource = context.getDirectEntity();
			PowerHolderComponent powerComponent = PowerHolderComponent.KEY.get(target);

			List<Identifier> sources = powerComponent.getSources(power);
			String joinedSources = sources
				.stream()
				.map(Identifier::toString)
				.collect(Collectors.joining(", "));

			if (sources.isEmpty()) {
				commandSource.sendError(Component.translatable("commands.apoli.sources.fail", target.getName(), power.getName()));
			}

			else {
				commandSource.sendFeedback(() -> Component.translatable("commands.apoli.sources.pass", target.getName(), sources.size(), power.getName(), joinedSources), false);
			}

			return sources.size();

		}

	}

	public static class RemoveNode {

		public static LiteralCommandNode<CommandSourceStack> get() {
			return literal("remove")
				.then(argument("targets", PowerHolderArgumentType.holders())
					.then(argument("power", PowerArgumentType.power())
						.suggests(PowerSuggestionProvider.powersFromEntities("targets"))
						.executes(RemoveNode::execute)
						.then(literal("")))).build();
		}

		public static int execute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {

			List<LivingEntity> targets = PowerHolderArgumentType.getHolders(context, "targets");
			Power power = PowerArgumentType.getPower(context, "power");

			CommandSourceStack commandSource = context.getDirectEntity();
			List<LivingEntity> processedTargets = new ObjectArrayList<>();

			for (LivingEntity target : targets) {

				Map<Identifier, Collection<Power>> powers = PowerHolderComponent.KEY.get(target).getSources(power)
					.stream()
					.collect(Collectors.toMap(Function.identity(), id -> ObjectOpenHashSet.of(power), MiscUtil.mergeCollections()));

				if (PowerHolderComponent.revokePowers(target, powers, true)) {
					processedTargets.add(target);
				}

			}

			if (processedTargets.isEmpty()) {

				if (targets.size() == 1) {
					commandSource.sendError(Component.translatable("commands.apoli.remove.fail.single", targets.getFirst().getName(), power.getName()));
				}

				else {
					commandSource.sendError(Component.translatable("commands.apoli.remove.fail.multiple", power.getName()));
				}

			}

			else {

				if (processedTargets.size() == 1) {
					commandSource.sendFeedback(() -> Component.translatable("commands.apoli.remove.success.single", processedTargets.getFirst().getName(), power.getName()), true);
				}

				else {
					commandSource.sendFeedback(() -> Component.translatable("commands.apoli.remove.success.multiple", processedTargets.size(), power.getName()), false);
				}

			}

			return processedTargets.size();

		}

	}

	public static class ClearNode {

		public static LiteralCommandNode<CommandSourceStack> get() {
			return literal("clear")
				.executes(context -> execute(context, true))
				.then(argument("targets", PowerHolderArgumentType.holders())
					.executes(context -> execute(context, false))).build();
		}

		public static int execute(CommandContext<CommandSourceStack> context, boolean self) throws CommandSyntaxException {

			List<Entity> targets = new ObjectArrayList<>();
			List<Entity> processedTargets = new ObjectArrayList<>();

			CommandSourceStack commandSource = context.getDirectEntity();
			AtomicInteger clearedPowers = new AtomicInteger();

			if (self) {

				Entity selfEntity = commandSource.getEntityOrThrow();

				PowerHolderComponent.KEY.maybeGet(selfEntity)
					.map(powerComponent -> targets.add(selfEntity))
					.orElseThrow(() -> PowerHolderArgumentType.HOLDER_NOT_FOUND.create(selfEntity.getName()));

			}

			else {
				targets.addAll(PowerHolderArgumentType.getHolders(context, "targets"));
			}

			for (Entity target : targets) {

				PowerHolderComponent component = PowerHolderComponent.KEY.get(target);
				List<Identifier> sources = component.getPowers(false)
					.stream()
					.map(component::getSources)
					.flatMap(Collection::stream)
					.toList();

				if (sources.isEmpty()) {
					continue;
				}

				clearedPowers.accumulateAndGet(PowerHolderComponent.revokeAllPowersFromAllSources(target, sources, true), Integer::sum);
				processedTargets.add(target);

			}

			if (processedTargets.isEmpty()) {

				if (targets.size() == 1) {
					commandSource.sendError(Component.translatable("commands.apoli.clear.fail.single", targets.getFirst().getName()));
				}

				else {
					commandSource.sendError(Component.translatable("commands.apoli.clear.fail.multiple"));
				}

			}

			else {

				if (processedTargets.size() == 1) {
					commandSource.sendFeedback(() -> Component.translatable("commands.apoli.clear.success.single", processedTargets.getFirst().getName(), clearedPowers.get()), true);
				}

				else {
					commandSource.sendFeedback(() -> Component.translatable("commands.apoli.clear.success.multiple", processedTargets.size(), clearedPowers.get()), true);
				}

			}

			return clearedPowers.get();

		}

	}

	public static class DumpNode {

		public static LiteralCommandNode<CommandSourceStack> get() {
			return literal("dump")
				.then(argument("power", PowerArgumentType.power())
					.executes(context -> execute(context, 4))
					.then(argument("indent", IntegerArgumentType.integer(0))
						.executes(context -> execute(context, IntegerArgumentType.getInteger(context, "indent"))))).build();
		}

		public static int execute(CommandContext<CommandSourceStack> context, int indent) throws CommandSyntaxException {

			Power power = PowerArgumentType.getPower(context, "power");
			CommandSourceStack commandSource = context.getDirectEntity();

			return Power.DATA_TYPE.write(commandSource.registryAccess().createSerializationContext(JsonOps.INSTANCE), power)
				.ifSuccess(powerJson -> commandSource.sendFeedback(() -> new JsonTextFormatter(indent).apply(powerJson), false))
				.ifError(error -> commandSource.sendError(Component.literal(error.message())))
				.mapOrElse(jsonElement -> 1, error -> 0);

		}

	}

}
