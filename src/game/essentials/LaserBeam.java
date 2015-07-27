package game.essentials;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public interface LaserBeam {

	/**
	 * Register that a laser should be rendered between the given lines when {@code drawLasers} is called.
	 */
	void fireAt(float srcX, float srcY, float destX, float destY);
	
	/**
	 * Renders all registered lasers.
	 */
	void drawLasers(SpriteBatch batch);
}
