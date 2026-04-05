package io.github.apace100.apoli.screen.toast;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.CustomToastData;
import io.github.apace100.apoli.util.TextureUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.toasts.ToastManager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.client.renderer.RenderPipelines;

import java.util.List;

@Environment(EnvType.CLIENT)
public class CustomToast implements PositionAwareToast {

    private static final int TITLE_BASE_COLOR = 16776960;
    private static final int DESCRIPTION_BASE_COLOR = 16777215;

    private final List<FormattedCharSequence> title;
    private final List<FormattedCharSequence> description;
    private final ItemStack iconStack;
    private final Identifier texture;

    private final int duration;

    private final int titleHeight;
    private final int descriptionHeight;

    private final int alphaShiftEnd;
    private final int heightShift;

    private int toastHeight;

    private Visibility wantedVisibility = Visibility.SHOW;
    private long lastStartTime;

    public CustomToast(CustomToastData toastData) {
        this(toastData.title(), toastData.description(), toastData.texture(), toastData.iconStack(), (int) ((toastData.duration() / 20.0) * 1000.0));
    }

    public CustomToast(Component title, Component description, Identifier texture, ItemStack iconStack, int duration) {

        Font textRenderer = Minecraft.getInstance().font;
        int maxWidth = this.width() - 33;

        this.title = textRenderer.split(title, maxWidth);
        this.description = textRenderer.split(description, maxWidth);
        this.iconStack = iconStack;
        this.duration = duration;

        this.titleHeight = 24 + (Math.max(this.title.size(), 1) * 8);
        this.descriptionHeight = 24 + (Math.max(this.description.size(), 1) * 8);

        this.toastHeight = titleHeight;
        this.texture = TextureUtil.tryLoadingTexture(texture)
            .result()
            .orElseGet(() -> TextureUtil
                .tryLoadingSprite(texture, TextureUtil.GUI_ATLAS_TEXTURE)
                .resultOrPartial(err -> Apoli.LOGGER.warn("Couldn't load texture \"{}\" as is, or as a sprite! Using default texture \"{}\" instead...", texture, CustomToastData.DEFAULT_TEXTURE))
                .orElse(CustomToastData.DEFAULT_TEXTURE));

        this.alphaShiftEnd = duration / 3;
        this.heightShift = duration / 2;

    }

    @Override
    public Visibility getWantedVisibility() {
        return wantedVisibility;
    }

    @Override
    public void update(ToastManager manager, long startTime) {
        this.lastStartTime = startTime;
        this.toastHeight = (int) Mth.lerp(Mth.clamp((float) startTime / (float) heightShift, 0F, 1F), titleHeight, descriptionHeight);
        this.wantedVisibility = startTime >= duration * manager.getNotificationDisplayTimeMultiplier()
            ? Visibility.HIDE
            : Visibility.SHOW;
    }

    @Override
    public void extractRenderStatePositionAware(int x, int y, GuiGraphicsExtractor context, Font textRenderer, long startTime) {

        int alphaShift = Mth.floor(Mth.clamp((float) Math.abs(alphaShiftEnd - startTime) / 300, 0.0, 1.0) * 255.0f) << 24 | 67108864;

        int toastTextX = 30;
        int toastTextYCenter = this.height() / 2;

        int titleY = toastTextYCenter - title.size() * 9 / 2;
        int descriptionY = toastTextYCenter - description.size() * 9 / 2;

        int titleYOffset = Math.max(7, titleY);
        int descriptionYOffset = Math.max(7, descriptionY);

        //  Draw the texture and icon of the toast
        context.blitSprite(RenderPipelines.GUI_TEXTURED, texture, 0, 0, this.width(), this.height());
        context.fakeItem(iconStack, 8, toastTextYCenter - 8);

        //  If the title and the description only has 1 line, display as is
        if (title.size() == 1 && description.size() == 1) {
            context.text(textRenderer, title.getFirst(), toastTextX, 7, TITLE_BASE_COLOR | 0xFF000000, false);
            context.text(textRenderer, description.getFirst(), toastTextX, 18, -1, false);
        }

        //  If the toast has only been displayed for a certain amount of time,
        //  display and fit the title texts onto the toast and shift its alpha channel (for the fade effect)
        else if (startTime < alphaShiftEnd) {

            context.enableScissor(x + 4, y + 4, x + this.width() - 4, y + this.height() - 4);

            for (var titleLine : title) {
                context.text(textRenderer, titleLine, toastTextX, titleYOffset, TITLE_BASE_COLOR | alphaShift, false);
                titleYOffset += 9;
            }

            context.disableScissor();


        }

        //  Otherwise, display and fit the description texts onto the toast
        else {

            context.enableScissor(x + 4, y + 4, x + this.width() - 4, y + this.height() - 4);

            for (var descriptionLine : description) {
                context.text(textRenderer, descriptionLine, toastTextX, descriptionYOffset, DESCRIPTION_BASE_COLOR | alphaShift, false);
                descriptionYOffset += 9;
            }

            context.disableScissor();

        }

    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, Font font, long startTime) {
        // Fallback when not called through the mixin; render at 0,0
        extractRenderStatePositionAware(0, 0, context, font, startTime);
    }

    @Override
    public int height() {
        return toastHeight;
    }

}
