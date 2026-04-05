package io.github.apace100.apoli.command.argument;

import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerManager;
import io.github.apace100.apoli.power.type.PowerType;
import io.github.apace100.apoli.util.PowerUtil;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public record PowerArgumentType(PowerTarget powerTarget) implements ArgumentType<Identifier> {

    public static final DynamicCommandExceptionType POWER_NOT_RESOURCE = new DynamicCommandExceptionType(
        o -> Component.translatableEscape("commands.apoli.power_not_resource", o)
    );

    public static final Dynamic2CommandExceptionType POWER_NOT_GRANTED = new Dynamic2CommandExceptionType(
        (a, b) -> Component.translatable("commands.apoli.power_not_granted", a, b)
    );

    public static final DynamicCommandExceptionType POWER_NOT_FOUND = new DynamicCommandExceptionType(
        o -> Component.translatableEscape("commands.apoli.power_not_found", o)
    );

    public static PowerArgumentType power() {
        return new PowerArgumentType(PowerTarget.GENERAL);
    }

    public static Power getPower(CommandContext<CommandSourceStack> context, String argumentName) throws CommandSyntaxException {
        Identifier powerId = context.getArgument(argumentName, Identifier.class);
        return PowerManager.getOptional(powerId).orElseThrow(() -> POWER_NOT_FOUND.create(powerId));
    }

    public static PowerArgumentType resource() {
        return new PowerArgumentType(PowerTarget.RESOURCE);
    }

    public static Power getResource(CommandContext<CommandSourceStack> context, String argumentName) throws CommandSyntaxException {
        Power power = getPower(context, argumentName);
        return PowerUtil.validateResource(power.getType())
            .map(PowerType::getPower)
            .getOrThrow(err -> POWER_NOT_RESOURCE.create(power.getId()));
    }

    @Override
    public Identifier parse(StringReader reader) throws CommandSyntaxException {
        return Identifier.read(reader);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {

        Stream<Identifier> powerIds = PowerManager.entrySet()
            .stream()
            .filter(e -> powerTarget() != PowerTarget.RESOURCE || PowerUtil.validateResource(e.getValue().getType()).isSuccess())
            .map(Map.Entry::getKey);

        return SharedSuggestionProvider.suggestResource(powerIds, builder);

    }

    public enum PowerTarget {
        GENERAL,
        RESOURCE
    }

    public record Serializer() implements ArgumentTypeInfo<PowerArgumentType, Serializer.Properties> {

        @Override
        public void serializeToNetwork(Properties properties, FriendlyByteBuf buf) {
            buf.writeVarInt(properties.powerTarget().ordinal());
        }

        @Override
        public Properties deserializeFromNetwork(FriendlyByteBuf buf) {
            return new Properties(this, PowerTarget.values()[buf.readVarInt()]);
        }

        @Override
        public void serializeToJson(Properties properties, JsonObject jsonObject) {
            jsonObject.addProperty("power_target", properties.powerTarget().name());
        }

        @Override
        public Properties unpack(PowerArgumentType argumentType) {
            return new Properties(this, argumentType.powerTarget());
        }

        public record Properties(Serializer serializer, PowerTarget powerTarget) implements ArgumentTypeInfo.Template<PowerArgumentType> {

            @Override
            public PowerArgumentType instantiate(CommandBuildContext commandRegistryAccess) {
                return new PowerArgumentType(powerTarget());
            }

            @Override
            public ArgumentTypeInfo<PowerArgumentType, ?> type() {
                return serializer();
            }

        }

    }

}
