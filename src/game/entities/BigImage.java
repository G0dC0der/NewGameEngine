package game.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import game.core.Entity;

public class BigImage extends Entity{

	public enum RenderStrategy{
		FULL_RENDER,
		FIXED,
		PORTION,
		REPEAT,
		PARALLAX,
		REPEAT_PARALLAX
	}
	
	@Override
	public void render(SpriteBatch batch) {
	}
}
