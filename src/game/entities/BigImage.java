package game.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

import game.core.Entity;
import game.essentials.Animation;
import game.essentials.Image2D;

public class BigImage extends Entity{

	public enum RenderStrategy{
		DEFAULT,
		FIXED,
		PORTION,
		REPEAT,
		PARALLAX,
		PARALLAX_REPEAT
	}
	
	private RenderStrategy strategy;
	private OrthographicCamera parallaxCamera;
	private float ratioX, ratioY, prevTx, prevTy;
	
	public void setRenderStrategy(RenderStrategy strategy){
		this.strategy = strategy;
		initParallaxCamer();
	}
	
	public RenderStrategy getRenderStrategy(){
		return strategy;
	}
	
	@Override
	public void setImage(Animation<Image2D> image) {
		super.setImage(image);
		initParallaxCamer();
	}
		
	@Override
	public void render(SpriteBatch batch) {
		if(getRotation() != 0 || scaleX != 1 || scaleY != 1)
			throw new RuntimeException("A big image must have rotation set to 0 and scaleX and scaleY set to 1.");
		
		Vector2 center = getCenterCord();
		Color defColor = batch.getColor();
		Color newColor = new Color(defColor);
		newColor.a = alpha;
		
		switch(strategy){
			case DEFAULT:
				//TODO:
				break;
			case PORTION:
				
		}
		
		batch.setColor(defColor);
	}

	public float getRatioX() {
		return ratioX;
	}

	public void setRatioX(float ratioX) {
		this.ratioX = ratioX;
	}

	public float getRatioY() {
		return ratioY;
	}

	public void setRatioY(float ratioY) {
		this.ratioY = ratioY;
	}
	
	private void initParallaxCamer(){
		if(strategy == RenderStrategy.PARALLAX){
			parallaxCamera = new OrthographicCamera(width(), height());
			parallaxCamera.setToOrtho(true);
			parallaxCamera.position.set(0, 0, 0);
		} else if(strategy == RenderStrategy.PARALLAX_REPEAT){
			parallaxCamera = new OrthographicCamera(getLevel().getWidth(), getLevel().getHeight());
			parallaxCamera.setToOrtho(true);
			parallaxCamera.position.set(0, 0, 0);
		}
	}
}
