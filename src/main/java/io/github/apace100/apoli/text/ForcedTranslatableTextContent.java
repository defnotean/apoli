package io.github.apace100.apoli.text;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.apace100.apoli.mixin.TranslatableTextContentAccessor;
import net.minecraft.network.chat.*;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.contents.TranslatableContents;

import java.util.List;
import java.util.Optional;

public class ForcedTranslatableTextContent extends TranslatableContents {

	public static final MapCodec<ForcedTranslatableTextContent> FORCED_TRANSLATABLE_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		Codec.STRING.fieldOf("translate").forGetter(ForcedTranslatableTextContent::getKey),
		ComponentSerialization.CODEC.fieldOf("alt_text").forGetter(ForcedTranslatableTextContent::getTextFallback),
		TranslatableTextContentAccessor.getArgumentCodec().listOf().optionalFieldOf("with").forGetter(content -> TranslatableTextContentAccessor.callToOptionalList(content.getArgs()))
	).apply(instance, ForcedTranslatableTextContent::new));

	// MC 26.1: ComponentContents.Type removed. Registration now uses MapCodec directly.

	private final Component textFallback;

	public ForcedTranslatableTextContent(String key, Component textFallback, Object... args) {
		super(key, null, args);
		this.textFallback = textFallback;
	}

	private ForcedTranslatableTextContent(String key, Component textFallback, Optional<List<Object>> args) {
		this(key, textFallback, TranslatableTextContentAccessor.callToArray(args));
	}

	@Override
	public MapCodec<TranslatableContents> codec() {
		// Safe cast: ForcedTranslatableTextContent extends TranslatableContents, and the codec handles the mapping
		@SuppressWarnings("unchecked")
		MapCodec<TranslatableContents> mapped = (MapCodec<TranslatableContents>) (MapCodec<?>) FORCED_TRANSLATABLE_CODEC;
		return mapped;
	}

	@Override
	protected void decompose() {

		Language language = Language.getInstance();
		if (language == this.decomposedWith) {
			return;
		}

		String key = this.getKey();
		String translated = language.getOrDefault(key);

		this.decomposedWith = language;

		if (language.has(key)) {

			try {

				ImmutableList.Builder<FormattedText> builder = ImmutableList.builder();
				((TranslatableTextContentAccessor) this).callForEachPart(translated, builder::add);

				this.decomposedParts = builder.build();

			}

			catch (IllegalArgumentException te) {
				this.decomposedParts = ImmutableList.of(FormattedText.of(translated));
			}

		}

		else {
			this.decomposedParts = ImmutableList.of(getTextFallback());
		}

	}

	public Component getTextFallback() {
		return textFallback;
	}

}
