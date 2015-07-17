package game.essentials;

import com.badlogic.gdx.math.Vector2;

import game.core.Collisions;
import game.core.Entity;
import game.core.MobileEntity;
import game.events.Event;

public class Factory {

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
}
