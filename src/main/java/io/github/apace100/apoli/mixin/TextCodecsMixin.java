package io.github.apace100.apoli.mixin;

import com.mojang.serialization.MapCodec;
import io.github.apace100.apoli.text.ForcedTranslatableTextContent;
import net.minecraft.network.chat.ComponentSerialization;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.ArrayList;
import java.util.Collection;

@Mixin(ComponentSerialization.class)
public abstract class TextCodecsMixin {

	@SuppressWarnings({"unchecked", "rawtypes"})
	@ModifyArg(method = "createCodec", at = @At(value = "NEW", target = "(Ljava/util/Collection;Ljava/util/function/Function;)Lnet/minecraft/network/chat/ComponentSerialization$FuzzyCodec;"), index = 0)
	private static Collection apoli$addCustomCodec(Collection original) {
		ArrayList codecs = new ArrayList(original);
		codecs.add(ForcedTranslatableTextContent.CODEC);
		return codecs;
	}

}
