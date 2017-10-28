package pojahn.game.entities;

import com.badlogic.gdx.graphics.Color;
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
        final Color orgColor = batch.getColor();
        final Color newColor = new Color(orgColor);
        newColor.a = (duration - framesAlive) * (1.0f / duration);

        batch.setColor(newColor);
        getEngine().hudCamera();
        batch.draw(flashImage, 0, 0, viewport.width, viewport.height);
        getEngine().gameCamera();
        batch.setColor(orgColor);
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
        return framesAlive++ > duration;
    }

    @Override
    protected void erupt() {}

    @Override
    protected void step() {}
}
