package game.essentials;

import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;

import game.core.Collisions;
import game.core.Engine;
import game.core.Entity;
import game.core.Level;
import game.core.Level.Tile;
import game.core.MobileEntity;
import game.core.PlayableEntity;
import game.events.Event;
import game.events.TileEvent;

public class Factory {
	
	public static MobileEntity cameraFocus(List<Entity> entities, int padding){
		Level level = entities.get(0).getLevel();
		Engine engine = level.getEngine();
		
		return new MobileEntity(){{
				zIndex(Integer.MAX_VALUE);
			}
			
			@Override
			public void logics() {
				Entity first = entities.get(0);
				float 	tx = 0,
						ty = 0,
						zoom = 0,
						stageWidth = level.getWidth(),
						stageHeight = level.getHeight(),
						windowWidth = Gdx.graphics.getWidth(),
						windowHeight = Gdx.graphics.getHeight();
				
				if(entities.size() == 1){
					tx = Math.min(stageWidth  - windowWidth,   Math.max(0, first.centerX() - windowWidth  / 2)) + windowWidth  / 2; 
					ty = Math.min(stageHeight - windowHeight,  Math.max(0, first.centerY() - windowHeight / 2)) + windowHeight / 2;
				}
				else if(entities.size() > 1){
					final float marginX = windowWidth  / 2;
					final float marginY = windowHeight / 2;
					
					float boxX	= first.x();
					float boxY	= first.y(); 
					float boxWidth	= boxX + first.width();
					float boxHeight	= boxY + first.height();

					for(int i = 1; i < entities.size(); i++){
						Entity focus = entities.get(i);
						
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
					
					engine.translate(tx, ty);
					engine.setZoom(zoom);
				}
			}
		};
	}

	/**
	 * Rotate the given unit towards its given direction.
	 */
	public static Event rotateTowards(MobileEntity walker){
		return ()->{
			Vector2 center = walker.getCenterCord();
			walker.setRotation((float)Collisions.getAngle(walker.prevX(), walker.prevY(), center.x, center.y));
		};
	}

	/**
	 * Rotate the given unit towards its given direction, flipping it horizontally when necessary.
	 */
	public static Event rotateSmartTowards(MobileEntity walker){	//TODO: Test it
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
	
	public static Event flip(MobileEntity entity){
		return ()-> {
			float diffX = entity.x() - entity.prevX();
			float diffY = entity.y() - entity.prevY();
			
			if(diffX != 0)
				entity.flipX = diffX > 0;
			if(diffY != 0)
				entity.flipY = diffY > 0;
		};
	}
	
	public static void rotate360(Entity entity, float speed){
		entity.getLevel().addTemp(()->{
			entity.rotate(speed);
		}, ()-> entity.getRotation() > 360);
	}
	
	public static void follow(Entity src, Entity tail, float offsetX, float offsetY){
		src.addEvent(()->{
			tail.bounds.x = src.bounds.x + offsetX;
			tail.bounds.y = src.bounds.y + offsetY;
		});
	}
	
	public static void follow(Entity src, Entity tail){
		follow(src, tail, 0, 0);
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
