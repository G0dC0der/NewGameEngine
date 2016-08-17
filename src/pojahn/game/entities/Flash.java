package pojahn.game.entities;

import java.awt.Dimension;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Flash extends Particle {

	private float duration, framesAlive;
	private Texture flashImage;

	{
		zIndex(Integer.MAX_VALUE);
	}

	public Flash(Texture flashImage, float duration) {
		this.duration = duration;
		this.flashImage = flashImage;
	}

    @Override
    public void logistics() {}

    @Override
	public void render(SpriteBatch batch) {
		if (framesAlive++ < duration) {
			Dimension viewport = getEngine().getScreenSize();
			Color orgColor = batch.getColor();
			Color newColor = new Color(orgColor);
			newColor.a = (duration - framesAlive) * (1.0f / duration);

			batch.setColor(newColor);
			getEngine().hudCamera();
			batch.draw(flashImage, 0, 0, viewport.width, viewport.height);
			getEngine().gameCamera();
			batch.setColor(orgColor);
		} else
			getLevel().discard(this);
	}

	@Override
	public Flash getClone() {
		Flash clone = new Flash(flashImage, duration);
		copyData(clone);

		if (cloneEvent != null)
			cloneEvent.handleClonded(clone);

		return clone;
	}
}
