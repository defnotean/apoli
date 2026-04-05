package io.github.apace100.apoli.mixin;

import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.RecordBuilder;
import io.github.apace100.apoli.text.ForcedTranslatableTextContent;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.util.StringRepresentable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Arrays;

@Mixin(ComponentSerialization.class)
public abstract class TextCodecsMixin {

	@ModifyArg(method = "createCodec", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/chat/ComponentSerialization;dispatchingCodec([Lnet/minecraft/util/StringRepresentable;Ljava/util/function/Function;Ljava/util/function/Function;Ljava/lang/String;)Lcom/mojang/serialization/MapCodec;"))
	private static StringRepresentable[] apoli$addCustomTypes(StringRepresentable[] original) {

		StringRepresentable[] copy = Arrays.copyOf(original, original.length + 1);
		copy[copy.length - 1] = ForcedTranslatableTextContent.TYPE;
		return copy;

	}

	@Mixin(targets = "net/minecraft/network/chat/ComponentSerialization$FuzzyCodec")
	public static abstract class FuzzyCodecMixin<T> {

		@Inject(method = "encode", at = @At(value = "INVOKE", target = "Lcom/mojang/serialization/MapEncoder;encode(Ljava/lang/Object;Lcom/mojang/serialization/DynamicOps;Lcom/mojang/serialization/RecordBuilder;)Lcom/mojang/serialization/RecordBuilder;"))
		private <S> void apoli$encodeType(T input, DynamicOps<S> ops, RecordBuilder<S> prefix, CallbackInfoReturnable<RecordBuilder<S>> cir) {

			//	Encode the text content's type to the result. This ensures that when sending a text to the client,
			//	the client will know what text content type to use to decode instead of relying on fuzzy matching
			if (input instanceof ComponentContents textContent) {
				prefix.add("type", ops.createString(textContent.getType().id()));
			}

		}

	}

}
