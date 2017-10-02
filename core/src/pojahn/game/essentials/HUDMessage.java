package pojahn.game.essentials;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.awt.*;

public interface HUDMessage {

    String getMessage();

    float getX();

    float getY();

    default Color getColor() {
        return Color.WHITE;
    }

    default void draw(final SpriteBatch batch, final BitmapFont font) {
        final Color orgColor = font.getColor();
        font.setColor(getColor());
        font.draw(batch, getMessage(), getX(), getY());
        font.setColor(orgColor);
    }

    public static HUDMessage getMessage(final String text, final float x, final float y, final Color color) {
        return new HUDMessage() {
            @Override
            public float getY() {
                return y;
            }

            @Override
            public float getX() {
                return x;
            }

            @Override
            public Color getColor() {
                return color == null ? Color.WHITE : color;
            }

            @Override
            public String getMessage() {
                return text;
            }
        };
    }

    public static HUDMessage centeredMessage(final String text, final Dimension screenSize, final Color color) {
        return new HUDMessage() {

            float x = -1, y = -1;

            @Override
            public float getY() {
                return y;
            }

            @Override
            public float getX() {
                return x;
            }

            @Override
            public String getMessage() {
                return text;
            }

            @Override
            public Color getColor() {
                return color == null ? Color.WHITE : color;
            }

            @Override
            public void draw(final SpriteBatch batch, final BitmapFont font) {
                if (x == -1 || y == -1)
                    calc(font);

                HUDMessage.super.draw(batch, font);
            }

            void calc(final BitmapFont font) {
                final GlyphLayout layout = new GlyphLayout();
                layout.setText(font, getMessage());
                final float width = layout.width;
                final float height = layout.height;

                x = (screenSize.width / 2) - (width / 2);
                y = (screenSize.height / 2) - (height / 2);
            }
        };
    }
}
