package pojahn.game.essentials;

import java.awt.Dimension;
import java.util.List;
import java.util.stream.Collectors;

import pojahn.game.core.Entity;
import pojahn.game.core.MobileEntity;

public class CameraEffects {

	public static MobileEntity cameraFocus(List<Entity> entities, int padding, boolean ignoreInvisible){
		return new MobileEntity(){{
				zIndex(10_000);
			}
			
			@Override
			public void logics() {
				Dimension size = getEngine().getScreenSize();
				float 	tx = 0,
						ty = 0,
						zoom = 0,
						stageWidth = getLevel().getWidth(),
						stageHeight = getLevel().getHeight(),
						windowWidth = size.width,
						windowHeight = size.height;
				
				if(entities.size() == 1){
					Entity first = entities.get(0);
					tx = Math.min(stageWidth  - windowWidth,   Math.max(0, first.centerX() - windowWidth  / 2)) + windowWidth  / 2; 
					ty = Math.min(stageHeight - windowHeight,  Math.max(0, first.centerY() - windowHeight / 2)) + windowHeight / 2;
					getEngine().translate(tx, ty);
				}
				else if(entities.size() > 1){
					List<Entity> list;
					if(ignoreInvisible){
						list = entities.stream().filter(entity -> entity.isActive() && entity.isVisible()).collect(Collectors.toList());
					}else
						list = entities;
					
					if(list.isEmpty())
						return;
					
					Entity first = list.get(0);
					
					final float marginX = windowWidth  / 2;
					final float marginY = windowHeight / 2;
					
					float boxX	= first.x();
					float boxY	= first.y(); 
					float boxWidth	= boxX + first.width();
					float boxHeight	= boxY + first.height();

					for(int i = 1; i < list.size(); i++){
						Entity focus = list.get(i);
						
						boxX = Math.min( boxX, focus.x() );
						boxY = Math.min( boxY, focus.y() );
						
						boxWidth  = Math.max( boxWidth,  focus.x() + focus.width () );
						boxHeight = Math.max( boxHeight, focus.y() + focus.height() );
					}
					boxWidth = boxWidth - boxX;
					boxHeight = boxHeight - boxY;
					
					boxX -= padding;
					boxY -= padding;
					boxWidth  += padding * 2;
					boxHeight += padding * 2;
					
					boxX = Math.max( boxX, 0 );
					boxX = Math.min( boxX, stageWidth - boxWidth ); 			

					boxY = Math.max( boxY, 0 );
					boxY = Math.min( boxY, stageHeight - boxHeight );
					
					if((float)boxWidth / (float)boxHeight > (float)windowWidth / (float)windowHeight)
						zoom = boxWidth / windowWidth;
					else
						zoom = boxHeight / windowHeight;
					
					zoom = Math.max( zoom, 1.0f );

					tx = boxX + ( boxWidth  / 2 );
					ty = boxY + ( boxHeight / 2 );
					
					if(marginX > tx)
						tx = marginX;
					else if(tx > stageWidth - marginX)
						tx = stageWidth - marginX;
					
					if(marginY > ty)
						ty = marginY;
					else if(ty > stageHeight - marginY)
						ty = stageHeight - marginY;
					
					getEngine().translate(tx, ty);
					getEngine().setZoom(zoom);
				}
			}
		};
	}
	
	/**
	 * Returns an {@code CameraEffect} event that moves vertically in a pingpong motion.<br>
	 * The function {@code isDone()} returns true either when the duration have elapsed or the event is manually stopped with the {@code stop()} function.
	 * @param length The length of the vertical movement.
	 * @param speed The speed of the vertical movement.
	 * @return The event.
	 */
	public static MobileEntity verticalMovement(float length, float speed){
		return pingPongMovement(length, speed, 0);
	}
	
	/**
	 * Returns an {@code CameraEffect} event that moves horizontally in a pingpong motion.
	 * <br>
	 * The function {@code isDone()} returns true either when the duration have elapsed or the event is manually stopped with the {@code stop()} function.
	 * @param length The length of the horizontal movement.
	 * @param speed The speed of the horizontal movement.
	 * @return The event.
	 */
	public static MobileEntity horizontalMovement(float length, float speed){
		return pingPongMovement(length, speed, 1);
	}
	
	/**
	 * Returns an {@code CameraEffect} event that zooms in and out in a pingpong motion with the given speed.<br>
	 * The zoom factor defaults to 1.0, which is 100%. Increasing the values zooms out rather than zooming in.<br>
	 * The function {@code isDone()} returns true either when the duration have elapsed or the event is manually stopped with the {@code stop()} function.
	 * @param min The minimum zoom. 
	 * @param max The maximum zoom.
	 * @param speed The speed of the pingpong motion.
	 * @return The event.
	 */
	public static MobileEntity zoomEffect(float min, float max, float speed){
		if(0 > min || 0 > max || 0 > speed)
			throw new IllegalArgumentException("All values must be positive.");
		
		return new MobileEntity(){
			
			float scaleValue;
			boolean increasingScale;
			
			@Override
			public void init() {
				scaleValue = getEngine().getZoom();
				zIndex(10_001);
			}
			
			@Override
			public void logics() {
				if(increasingScale) {
					scaleValue += speed;
					if(scaleValue > max)
						increasingScale = false;
				} else {
					scaleValue -= speed;
					if(scaleValue < min)
						increasingScale = true;
				}
				
				getEngine().setZoom(scaleValue);
			}
		};
	}
	
	/**
	 * Vibrates the screen for the specified amount of frames.<br>
	 * The function {@code isDone()} returns true either when the duration have elapsed or the event is manually stopped with the {@code stop()} function.
	 * @param strength The strength of the vibration.
	 * @return The event.
	 */
	public static MobileEntity vibration(float strength){
		return new MobileEntity(){
			int counter;
			
			{
				zIndex(10_003);
			}
			
			@Override
			public void logics() {
				int value = counter++ % 4;
				float tx = getEngine().tx();
				float ty = getEngine().ty();
				
				switch(value){
					case 0:
						tx += -strength;
						ty += -strength;
						break;
					case 1:
						tx += strength;
						ty += -strength;
						break;
					case 2:
						tx += strength;
						ty += strength;
						break;
					case 3:
						tx -= strength;
						ty += strength;
						break;
				}
				getEngine().translate(tx, ty);
			}
		};
	}
	
	static MobileEntity pingPongMovement(float length, float speed, int axis)
	{
		if(0 > length || 0 > speed)
			throw new IllegalArgumentException("Both values must be positive.");
		
		return new MobileEntity(){
			boolean increasingVert;
			float vertValue, vertLength, vertSpeed;
			
			{
				if(speed == 0)
					vertValue = 0;
				
				vertLength = length;
				vertSpeed = speed;
			}
			
			@Override
			public void logics() {
				if(vertSpeed > 0) {
					if(increasingVert) {
						vertValue += vertSpeed;
						if(vertValue > vertLength)
							increasingVert = false;
					} else {
						vertValue -= vertSpeed;
						if(vertValue < -vertLength)
							increasingVert = true;
					}
					
					float tx = getEngine().tx();
					float ty = getEngine().ty();
					if(axis == 0)
						ty += vertValue;
					else if(axis == 1)
						tx += vertValue;
					
					getEngine().translate(tx, ty);
				}
			}
		};
	}
}
