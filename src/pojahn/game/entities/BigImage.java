package pojahn.game.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import pojahn.game.core.Engine;
import pojahn.game.core.Entity;
import pojahn.game.essentials.Animation;
import pojahn.game.essentials.Image2D;

import java.awt.*;

public class BigImage extends Entity{

	public enum RenderStrategy{
		FULL,
		FIXED,
		PORTION,
		REPEAT,
		PARALLAX,
		PARALLAX_REPEAT
	}
	
	private RenderStrategy strategy;
	private OrthographicCamera parallaxCamera;
	private float ratioX, ratioY, currRotation;
	private boolean initCamera;

	public BigImage(){
		this(RenderStrategy.FULL);
	}
	
	public BigImage(RenderStrategy strategy){
		this.strategy = strategy;
		initCamera = true;
	}
	
	public void setRenderStrategy(RenderStrategy strategy){
		this.strategy = strategy;
		initCamera = true;
	}
	
	public RenderStrategy getRenderStrategy(){
		return strategy;
	}
	
	@Override
	public void setImage(Animation<Image2D> image) {
		super.setImage(image);
		initCamera = true;
	}
		
	@Override
	public void render(SpriteBatch batch) {
		if(getRotation() != 0)
			throw new RuntimeException("A big image can not be rotated.");
		
		if(initCamera){
			initCamera = false;
			initParallaxCamera();
		}
		
		Engine e = getLevel().getEngine();
		Dimension screen = e.getScreenSize();
		Color defColor = batch.getColor();
		Color newColor = new Color(defColor.r, defColor.g, defColor.b, alpha);
		
		switch(strategy){
			case FULL:
				super.render(batch);
				break;
			case PORTION:
				float startX 	= e.tx() - screen.width  / 2;
				float startY 	= e.ty() - screen.height / 2;
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
				break;
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
	
	public void setRatio(float ratio){
		ratioX = ratioY = ratio;
	}
	
	private void initParallaxCamera(){
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
	
	private void updateParallaxCamera(){
		Engine e = getEngine();
		Dimension screen = e.getScreenSize();
		float rotation = e.getRotation();
		
		parallaxCamera.rotate(-currRotation);
		parallaxCamera.rotate(rotation);
		parallaxCamera.zoom = getEngine().getZoom();
		parallaxCamera.position.x = Math.max(screen.width  / 2, parallaxCamera.position.x + (e.tx() - e.prevTx()) * ratioX);
		parallaxCamera.position.y = Math.max(screen.height / 2, parallaxCamera.position.y + (e.ty() - e.prevTy()) * ratioY);
		parallaxCamera.update();
		
		currRotation = rotation;
	}
	
	private void repeat(SpriteBatch batch){
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
}
