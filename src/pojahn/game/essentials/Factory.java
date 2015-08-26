package pojahn.game.essentials;

import pojahn.game.core.Collisions;
import pojahn.game.core.Entity;
import pojahn.game.core.Level.Tile;
import pojahn.game.core.MobileEntity;
import pojahn.game.core.PlayableEntity;
import pojahn.game.events.Event;
import pojahn.game.events.TileEvent;

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
