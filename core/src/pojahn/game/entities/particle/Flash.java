package pojahn.game.entities.particle;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.awt.*;

public class Flash extends Particle {

    private final float duration;
    private final Texture flashImage;
    private float framesAlive;

    public Flash(final Texture flashImage, final float duration) {
        this.duration = duration;
        this.flashImage = flashImage;
        zIndex(Integer.MAX_VALUE);
    }

    @Override
    public void render(final SpriteBatch batch) {
        final Dimension viewport = getEngine().getScreenSize();
        tint.a = Math.max(0, duration - framesAlive) * (1.0f / duration);

        getEngine().hudCamera();
        batch.draw(flashImage, 0, 0, viewport.width, viewport.height);
        getEngine().gameCamera();
    }

    @Override
    public Flash getClone() {
        final Flash clone = new Flash(flashImage, duration);
        copyData(clone);

        if (cloneEvent != null)
            cloneEvent.handleClonded(clone);

        return clone;
    }

    @Override
    protected boolean completed() {
        return framesAlive++ > duration || tint.a <= 0.0f;
    }
}
