package pojahn.game.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import pojahn.game.core.Entity;

public class BackgroundImage {

    public static Entity parallaxImage(final float rateX, final float rateY, final boolean repeat) {
        return new ParallaxImage(rateX, rateY, repeat);
    }

    private static class ParallaxImage extends Entity {

        private final float rateX;
        private final float rateY;
        private final boolean repeat;

        private ParallaxImage(final float rateX, final float rateY, final boolean repeat) {
            this.rateX = rateX;
            this.rateY = rateY;
            this.repeat = repeat;
        }

        @Override
        public void logistics() {
            final float x = (getEngine().tx() - getEngine().prevTx()) * rateX;
            final float y = (getEngine().ty() - getEngine().prevTy()) * rateY;

            bounds.pos.x += x;
            bounds.pos.y += y;

            bounds.pos.x = Math.max(0, x());
            bounds.pos.y = Math.max(0, y());

            bounds.pos.x = Math.min(x(), getLevel().getWidth());
            bounds.pos.y = Math.min(y(), getLevel().getHeight());
        }

        @Override
        public void render(final SpriteBatch batch) {
            if (repeat) {
                final Color defColor = batch.getColor();
                batch.setColor(tint);


                batch.setColor(defColor);
            } else {
                super.render(batch);
            }
        }
    }
}
