package io.github.apace100.apoli.power.type;

import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.access.OverlaySpriteHolder;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.power.PowerConfiguration;
import io.github.apace100.apoli.util.MiscUtil;
import io.github.apace100.apoli.util.TextureUtil;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.renderer.*;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.client.resources.metadata.texture.TextureMetadataSection;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.Set;

//  TODO: Drop the old 'texture' field -eggohito
public class OverlayPowerType extends PowerType {

    public static final Identifier ATLAS_TEXTURE = Apoli.identifier("textures/atlas/overlay.png");

    public static final TypedDataObjectFactory<OverlayPowerType> DATA_FACTORY = createConditionedDataFactory(
        new SerializableData()
            .add("texture", SerializableDataTypes.IDENTIFIER, null)
            .addFunctionedDefault("sprite", SerializableDataTypes.IDENTIFIER, data -> data.get("texture"))
            .add("draw_mode", SerializableDataType.enumValue(DrawMode.class))
            .add("draw_phase", SerializableDataType.enumValue(DrawPhase.class))
            .add("strength", ApoliDataTypes.NORMALIZED_FLOAT, 1.0F)
            .add("red", ApoliDataTypes.NORMALIZED_FLOAT, 1.0F)
            .add("green", ApoliDataTypes.NORMALIZED_FLOAT, 1.0F)
            .add("blue", ApoliDataTypes.NORMALIZED_FLOAT, 1.0F)
            .add("hide_with_hud", SerializableDataTypes.BOOLEAN, true)
            .add("visible_in_third_person", SerializableDataTypes.BOOLEAN, false)
            .add("priority", SerializableDataTypes.INT, 0)
            .validate(MiscUtil.validateAnyFieldsPresent("texture", "sprite")),
        (data, condition) -> new OverlayPowerType(
            data.get("sprite"),
            data.get("draw_mode"),
            data.get("draw_phase"),
            data.get("strength"),
            data.get("red"),
            data.get("green"),
            data.get("blue"),
            data.get("hide_with_hud"),
            data.get("visible_in_third_person"),
            data.get("priority"),
            condition
        ),
        (powerType, serializableData) -> serializableData.instance()
            .set("sprite", powerType.spriteId)
            .set("draw_mode", powerType.drawMode)
            .set("draw_phase", powerType.drawPhase)
            .set("strength", powerType.strength)
            .set("red", powerType.red)
            .set("green", powerType.green)
            .set("blue", powerType.blue)
            .set("hide_with_hud", powerType.doesHideWithHud())
            .set("visible_in_third_person", powerType.shouldBeVisibleInThirdPerson())
            .set("priority", powerType.getPriority())
    );

    private final Identifier spriteId;

    private final DrawMode drawMode;
    private final DrawPhase drawPhase;

    private final float strength;
    private final float red;
    private final float green;
    private final float blue;

    private final boolean hideWithHud;
    private final boolean visibleInThirdPerson;

    private final int priority;

    private boolean initRender = true;
    private boolean invalidTexture;

    public OverlayPowerType(Identifier spriteId, DrawMode drawMode, DrawPhase drawPhase, float strength, float red, float green, float blue, boolean hideWithHud, boolean visibleInThirdPerson, int priority, Optional<EntityCondition> condition) {
        super(condition);
        this.spriteId = spriteId;
        this.drawMode = drawMode;
        this.drawPhase = drawPhase;
        this.hideWithHud = hideWithHud;
        this.visibleInThirdPerson = visibleInThirdPerson;
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.strength = strength;
        this.priority = priority;
    }

    @Override
    public @NotNull PowerConfiguration<?> getConfig() {
        return PowerTypes.OVERLAY;
    }

    public DrawPhase getDrawPhase() {
        return drawPhase;
    }

    public boolean shouldBeVisibleInThirdPerson() {
        return visibleInThirdPerson;
    }

    public boolean doesHideWithHud() {
        return hideWithHud;
    }

    public int getPriority() {
        return priority;
    }

    @Environment(EnvType.CLIENT)
    public boolean shouldRender(Options options, DrawPhase targetDrawPhase) {
        return this.getDrawPhase() == targetDrawPhase
            && (!options.hideGui || !this.doesHideWithHud())
            && (options.getCameraType().isFirstPerson() || this.shouldBeVisibleInThirdPerson());
    }

    @SuppressWarnings("SwitchStatementWithTooFewBranches")
    @Environment(EnvType.CLIENT)
    public void render() {

        if (spriteId == null || !initRender && invalidTexture) {
            return;
        }

        Minecraft client = Minecraft.getInstance();
        if (!(client instanceof OverlaySpriteHolder overlaySpriteHolder)) {
            return;
        }

        if (initRender) {

            this.invalidTexture = TextureUtil.tryLoadingSprite(spriteId, ATLAS_TEXTURE)
                .result()
                .isEmpty();
            this.initRender = false;

            if (invalidTexture) {
                Apoli.LOGGER.warn("Power \"{}\" references texture sprite \"{}\", which doesn't exist!", getPower().getId(), spriteId);
                return;
            }

        }

        // TODO: MC 26.1 completely overhauled the rendering pipeline. RenderSystem.disableDepthTest,
        // RenderSystem.enableBlend, RenderSystem.setShaderColor, RenderSystem.setShaderTexture,
        // GlStateManager.SrcFactor/DstFactor, and BufferBuilder.drawWithShader are all removed.
        // The overlay rendering needs to be reimplemented using the new RenderPipeline system.
        // For now, this is stubbed out to allow compilation.
        Apoli.LOGGER.debug("Overlay rendering is not yet implemented for MC 26.1");

    }

    @Environment(EnvType.CLIENT)
    public static final class SpriteHolder extends TextureAtlas {

        public SpriteHolder(TextureManager manager) {
            super(ATLAS_TEXTURE);
        }

        @Override
        public TextureAtlasSprite getSprite(Identifier objectId) {
            return super.getSprite(objectId);
        }

    }

    @Environment(EnvType.CLIENT)
    public static void integrateCallback(Minecraft client, boolean initialized) {
        PowerHolderComponent.getPowerTypes(client.player, OverlayPowerType.class, true).forEach(p -> p.initRender = true);
    }

    public enum DrawMode {
        NAUSEA, TEXTURE
    }

    public enum DrawPhase {
        BELOW_HUD, ABOVE_HUD
    }

}
