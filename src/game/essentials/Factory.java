package game.essentials;

import game.core.Collisions;
import game.core.Entity;
import game.core.Level.Tile;
import game.core.MobileEntity;
import game.core.PlayableEntity;
import game.events.Event;
import game.events.TileEvent;

import java.awt.Dimension;
import java.util.List;
import java.util.stream.Collectors;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

public class Factory {
	
	public static MobileEntity drawText(HUDMessage message, BitmapFont font){
		return new MobileEntity(){
			@Override
			public void render(SpriteBatch batch) {
				getEngine().hudCamera();
				message.draw(batch, font);
				getEngine().gameCamera();
			}
		};
	}
	
	public static MobileEntity cameraFocus(List<Entity> entities, int padding, boolean ignoreInvisible){//TODO: Do not use this as an event.
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
	 * Rotate the given unit towards its given direction.
	 */
	public static Event faceTarget(MobileEntity walker){
		return ()->{
			Vector2 center = walker.getCenterCord();
			walker.setRotation((float)Collisions.getAngle(walker.prevX(), walker.prevY(), center.x, center.y));
		};
	}

	/**
	 * Rotate the given unit towards its given direction, flipping it horizontally when necessary.
	 */
	public static Event smartFaceTarget(MobileEntity walker){	//TODO: Test it
		return ()->{
			
			Vector2 center = walker.getCenterCord();
			float orgRotation = walker.getRotation();
			float rotation = (float)Collisions.getAngle(walker.prevX(), walker.prevY(), center.x, center.y);
			
			if(orgRotation == rotation)
				return;
			
			if(rotation > 180.0f){
				walker.flipX = true;
				walker.setRotation(360 - rotation);
			} else{
				walker.flipX = false;
				walker.setRotation(rotation);
			}
		};
	}
	
	public static Event simpleFaceTarget(MobileEntity entity){
		return ()-> {
			float diffX = entity.x() - entity.prevX();
			float diffY = entity.y() - entity.prevY();
			
			if(diffX != 0)
				entity.flipX = diffX < 0;
			if(diffY != 0)
				entity.flipY = diffY > 0;
		};
	}
	
	public static Event follow(Entity src, Entity tail, float offsetX, float offsetY){
		return ()-> tail.move(src.bounds.pos.x + offsetX, src.bounds.pos.y + offsetY);
	}
	
	public static Event follow(Entity src, Entity tail){
		return follow(src, tail, 0, 0);
	}
	
	public static TileEvent crushable(PlayableEntity entity){
		return (tile) ->{
			if(tile == Tile.SOLID)
				entity.setState(Vitality.DEAD);
		};
	}
	
	public static TileEvent completable(PlayableEntity entity){
		return (tile)->{
			if(tile == Tile.GOAL)
				entity.setState(Vitality.COMPLETED);
		};
	}
	
	public static TileEvent hurtable(PlayableEntity entity, int damage){
		return (tile)->{
			if(tile == Tile.LETHAL)
				entity.touch(damage);
		};
	}
	
	public static Event hitMain(Entity enemy, PlayableEntity play, int hp){
		return ()->{
			if(enemy.collidesWith(play))
				play.touch(hp);
		};
	}
}
