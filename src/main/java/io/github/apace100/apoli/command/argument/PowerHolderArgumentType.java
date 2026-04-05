package io.github.apace100.apoli.command.argument;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

import java.util.LinkedList;
import java.util.List;

public class PowerHolderArgumentType extends EntityArgument {

    public static final SimpleCommandExceptionType HOLDERS_NOT_FOUND = new SimpleCommandExceptionType(
        Component.translatable("argument.apoli.power_holder.not_found.multiple")
    );

    public static final DynamicCommandExceptionType HOLDER_NOT_FOUND = new DynamicCommandExceptionType(
        o -> Component.translatable("argument.apoli.power_holder.not_found.single", o)
    );

    protected PowerHolderArgumentType(boolean singleTarget) {
        super(singleTarget, false);
    }

    public static PowerHolderArgumentType holder() {
        return new PowerHolderArgumentType(true);
    }

    public static PowerHolderArgumentType holders() {
        return new PowerHolderArgumentType(false);
    }

    public static LivingEntity getHolder(CommandContext<CommandSourceStack> context, String name) throws CommandSyntaxException {

        Entity entity = getEntity(context, name);
        if (!(entity instanceof LivingEntity livingEntity)) {
            throw HOLDER_NOT_FOUND.create(entity.getName());
        }

        return livingEntity;

    }

    public static List<LivingEntity> getHolders(CommandContext<CommandSourceStack> context, String name) throws CommandSyntaxException {

        List<? extends Entity> entities = new LinkedList<>(getEntities(context, name));
        List<LivingEntity> holders = entities.stream()
            .filter(e -> e instanceof LivingEntity)
            .map(e -> (LivingEntity) e)
            .toList();

        if (holders.isEmpty()) {

            if (entities.size() == 1) {
                throw HOLDER_NOT_FOUND.create(entities.getFirst().getName());
            }

            else {
                throw HOLDERS_NOT_FOUND.create();
            }

        }

        return holders;

    }

}
