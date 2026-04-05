package io.github.apace100.apoli.mixin;

import com.mojang.brigadier.arguments.ArgumentType;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.command.argument.PowerArgumentType;
import io.github.apace100.apoli.command.argument.PowerHolderArgumentType;
import io.github.apace100.apoli.command.argument.PowerOperationArgumentType;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ArgumentTypeInfos.class)
public abstract class ArgumentTypesMixin {
    @Shadow
    private static <A extends ArgumentType<?>, T extends ArgumentTypeInfo.Template<A>> ArgumentTypeInfo<A, T> register(Registry<ArgumentTypeInfo<?, ?>> registry, String string, Class<? extends A> clazz, ArgumentTypeInfo<A, T> argumentSerializer) {
        throw new AssertionError("Mixins for basic functionality are fun.");
    }
    @Inject(method = "register(Lnet/minecraft/core/Registry;)Lnet/minecraft/command/argument/serialize/ArgumentTypeInfo;", at = @At("RETURN"))
    private static void registerApoliArgumentTypes(Registry<ArgumentTypeInfo<?, ?>> registry, CallbackInfoReturnable<ArgumentTypeInfo<?, ?>> cir) {
        register(registry, Apoli.MODID + ":power", PowerArgumentType.class, new PowerArgumentType.Serializer());
        register(registry, Apoli.MODID + ":power_operation", PowerOperationArgumentType.class, SingletonArgumentInfo.of(PowerOperationArgumentType::operation));
        register(registry , Apoli.MODID + ":power_holder", PowerHolderArgumentType.class, new EntityArgument.Serializer());
    }
}
