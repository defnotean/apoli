package io.github.apace100.apoli.screen.widget;

import io.github.apace100.apoli.util.TextAlignment;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.components.AbstractStringWidget;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class ScrollingTextWidget extends AbstractStringWidget {

    private TextAlignment textAlignment = TextAlignment.CENTER;
    private final boolean hasShadow;
    private int color = 0xFFFFFFFF;

    public ScrollingTextWidget(int x, int y, int width, int height, Component text, boolean hasShadow, Font textRenderer) {
        super(x, y, width, height, text, textRenderer);
        this.hasShadow = hasShadow;
    }

    public void setAlignment(TextAlignment textAlignment) {
        this.textAlignment = textAlignment;
    }

    public void setColor(int color) {
        this.color = color;
    }

    @Override
    public void visitLines(ActiveTextCollector collector) {
        int left = this.getX() + 2;
        int right = this.getX() + this.getWidth() - 2;
        int top = this.getY();
        int bottom = this.getY() + this.getHeight();

        Font font = getFont();
        Component text = this.getMessage();
        int textWidth = font.width(text);
        int width = right - left;

        if (textWidth <= width) {
            // Text fits -- use static alignment
            java.util.Optional<Integer> horizontalAlignment = textAlignment.horizontal(left, right, textWidth);
            if (horizontalAlignment.isPresent()) {
                int height = (top + bottom - 9) / 2 + 1;
                collector.accept(horizontalAlignment.get(), height, text.getVisualOrderText());
            }
        } else {
            // Text overflows -- use built-in scrolling
            int height = (top + bottom - 9) / 2 + 1;
            collector.acceptScrolling(text, left, height, right, bottom, color);
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return false;
    }

}
