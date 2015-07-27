package game.entities;

import java.awt.Dimension;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

import game.core.Engine;
import game.core.Entity;
import game.core.Level;
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
	private float ratioX, ratioY;
	
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
		
		Level level = getLevel();
		Engine e = level.getEngine();
		Vector2 t = getEngine().getTranslation();
		Dimension screen = e.getScreenSize();
		Color defColor = batch.getColor();
		Color newColor = new Color(defColor.r, defColor.g, defColor.b, alpha);
		
		switch(strategy){
			case DEFAULT:
				super.render(batch);
				break;
			case PORTION:
				float startX 	= t.x - screen.width  / 2;
				float startY 	= t.y - screen.height / 2;
				float width 	= screen.width;
				float height 	= screen.height;
				float u			= startX / width();
				float v 		= startY / height();
				float u2		= (startX + width ) / width();
				float v2		= (startY + height) / height();
				batch.setColor(newColor);
				batch.draw(nextImage(), startX, startY, width, height, u, v, u2, v2);
				batch.setColor(defColor);
				break;
			case FIXED:
				e.hudCamera();
				super.render(batch);
				e.gameCamera();
			case REPEAT:
				batch.setColor(newColor);
				repeat(batch);
				batch.setColor(defColor);
				break;
			case PARALLAX:
				updateParallaxCamera();
				batch.setProjectionMatrix(parallaxCamera.combined);
				super.render(batch);
				e.gameCamera();
				break;
			case PARALLAX_REPEAT:
				updateParallaxCamera();
				batch.setProjectionMatrix(parallaxCamera.combined);
				repeat(batch);
				e.gameCamera();
				break;
		}
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
	
	private void updateParallaxCamera()
	{
		Engine e = getEngine();
		Dimension screen = e.getScreenSize();
		Vector2 t = e.getTranslation();
		Vector2 prevT = e.getPreviousTranslation();
		
		parallaxCamera.zoom = getScale();
		parallaxCamera.position.x = Math.max(screen.width  / 2, parallaxCamera.position.x + (t.x - prevT.x) * ratioX);
		parallaxCamera.position.y = Math.max(screen.height / 2, parallaxCamera.position.y + (t.y - prevT.y) * ratioY);
		parallaxCamera.update();
	}
	
	private void repeat(SpriteBatch batch)
	{
		int stageWidth  = getLevel().getWidth();
		int stageHeight = getLevel().getHeight();
		int repeatX = (int) (stageWidth /  width());
		int repeatY = (int) (stageHeight / height());
		
		if(stageWidth  > repeatX * width())
			repeatX++;
		if(stageHeight > repeatY * height())
			repeatY++;
			
		for(int x = 0; x < repeatX; x++)
			for(int y = 0; y < repeatY; y++)
				batch.draw(nextImage(), x * width(), y * height());
	}
	
	private float getScale()
	{
		float scale = getEngine().getZoom();
		
		if(scale == 1f)
			return scale;
		else if(scale > 1f)
			return scale - 1f;
		else
			return scale + 1f;
	}
}
