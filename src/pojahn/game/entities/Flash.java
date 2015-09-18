package pojahn.game.entities;

import java.awt.Dimension;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import pojahn.game.core.Engine;

public class Flash extends Particle {

	private float duration, framesAlive;
	private Texture flashImage;

	{
		zIndex(10_000);
	}

	public Flash(Color color, float duration) {
		this.duration = duration;

		Pixmap px = new Pixmap(1, 1, Format.RGBA8888);
		px.setColor(color);
		px.fill();

		this.flashImage = new Texture(px);
		px.dispose();
	}

	public Flash(Texture flashImage, float duration) {
		this.duration = duration;
		this.flashImage = flashImage;
	}

	@Override
	public void render(SpriteBatch batch) {
		if (framesAlive < duration) {
			framesAlive++;

			Engine eng = getEngine();
			Dimension viewport = eng.getScreenSize();
			Color orgColor = batch.getColor();
			Color newColor = new Color(orgColor);
			newColor.a = (duration - framesAlive) * (1.0f / duration);

			batch.setColor(newColor);
			eng.hudCamera();
			batch.draw(flashImage, 0, 0, viewport.width, viewport.height);
			eng.gameCamera();
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
