package io.github.apace100.apoli.power.type;

import com.mojang.datafixers.util.Either;
import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.power.PowerConfiguration;
import io.github.apace100.apoli.util.MiscUtil;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Gui;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;

//  TODO: Drop backwards compatibility with the old sprite sheet functionality -eggohito
public class OverrideHudTexturePowerType extends PowerType implements Prioritized<OverrideHudTexturePowerType> {

    public static final TypedDataObjectFactory<OverrideHudTexturePowerType> DATA_FACTORY = createConditionedDataFactory(
        new SerializableData()
            .add("texture", SerializableDataTypes.IDENTIFIER, null)
            .add("texture_map", ApoliDataTypes.IDENTIFIER_MAP, null)
            .add("priority", SerializableDataTypes.INT, 0)
            .validate(MiscUtil.validateAnyFieldsPresent("texture", "texture_map")),
        (data, condition) -> {

            Either<Identifier, Map<Identifier, Identifier>> textureOrMapping = data.isPresent("texture")
                ? Either.left(data.get("texture"))
                : Either.right(data.get("texture_map"));

            return new OverrideHudTexturePowerType(
                textureOrMapping,
                data.get("priority"),
                condition
            );

        },
        (powerType, serializableData) -> {

            SerializableData.Instance data = serializableData.instance();
            powerType.textureOrMapping
                .ifLeft(id -> data.set("texture", id))
                .ifRight(mapping -> data.set("texture_map", mapping));

            return data.set("priority", powerType.getPriority());

        }
    );

    private final Either<Identifier, Map<Identifier, Identifier>> textureOrMapping;
    private final int priority;

    public OverrideHudTexturePowerType(Either<Identifier, Map<Identifier, Identifier>> textureOrMapping, int priority, Optional<EntityCondition> condition) {
        super(condition);
        this.textureOrMapping = textureOrMapping;
        this.priority = priority;
    }

    @Override
    public @NotNull PowerConfiguration<?> getConfig() {
        return PowerTypes.STATUS_BAR_TEXTURE;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Environment(EnvType.CLIENT)
    public void drawHeartTexture(GuiGraphicsExtractor extractor, Gui.HeartType heartType, int x, int y, int width, int height, boolean hardcore, boolean blinking, boolean half) {

        textureOrMapping.ifRight(mapping -> {

            Identifier texture = heartType.getSprite(hardcore, half, blinking);
            Identifier newTexture = mapping.getOrDefault(texture, texture);

            extractor.blitSprite(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, newTexture, x, y, width, height);

        });

    }

    @Environment(EnvType.CLIENT)
    public void drawTextureRegion(GuiGraphicsExtractor extractor, Identifier texture, int width, int height, int minU, int minV, int legacyMinU, int legacyMinV, int x, int y, int maxU, int maxV) {
        textureOrMapping
            .ifRight(mapping -> extractor.blitSprite(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, mapping.getOrDefault(texture, texture), width, height, minU, minV, x, y, maxU, maxV));
    }

    @Environment(EnvType.CLIENT)
    public void drawTexture(GuiGraphicsExtractor extractor, Identifier texture, int x, int y, int legacyU, int legacyV, int width, int height) {
        textureOrMapping
            .ifRight(mapping -> extractor.blitSprite(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, mapping.getOrDefault(texture, texture), x, y, width, height));
    }

}
