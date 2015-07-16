package game.core;

import java.util.List;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import game.essentials.Hitbox;
import game.essentials.Image2D;
import game.events.Event;

public class Entity {

	public final Rectangle bounds;
	public String id;
	
	Level level;
	Engine engine;
	List<Event> events, deleteEvents;
	
	private Animation<Image2D> image;
	private Hitbox hitbox;
	private Polygon poly;
	private boolean quickCollision;
	private float rotation, offsetX, offsetY;
	private int zIndex;
	
	public Entity(){
		bounds = new Rectangle();
	}
	
	public void init() {}
	
	public void dispose() {}
	
	public void render(SpriteBatch batch){
		//Do the rendering here instead of the engine class
		//Override and call super for extra effect
	}
	
	public void zIndex(int zIndex){
		this.zIndex = zIndex;
		level.sort = true;
	}
	
	public int getZIndex(){
		return zIndex;
	}
	
	public Level getLevel(){
		return level;
	}
	
	public void addEvent(Event event){
		events.add(event);
	}
	
	public void removeEvent(Event event){
		deleteEvents.remove(event);
	}
	
	public boolean collidesWith(Entity entity){
		boolean rotated1 = rotation != 0 && !quickCollision;
		boolean rotated2 = entity.rotation != 0 && !entity.quickCollision;
		
		if(hitbox == Hitbox.NONE || entity.hitbox == Hitbox.NONE){
			return false;
		} else if(hitbox == Hitbox.RECTANGLE && entity.hitbox == Hitbox.RECTANGLE){
			if(rotated1 || rotated2)
				return false;//TODO:
			else
				return false;//TODO:
		} else if((hitbox == Hitbox.RECTANGLE && entity.hitbox == Hitbox.CIRCLE) || (hitbox == Hitbox.CIRCLE && entity.hitbox == Hitbox.RECTANGLE)){
			Entity rectangle 	= hitbox == Hitbox.RECTANGLE 	? this : entity;
			Entity circle 		= hitbox == Hitbox.CIRCLE 		? this : entity;
			
			if(rectangle.rotation != 0 && !rectangle.quickCollision)
				return false;//TODO
			else
				return false;//TODO:
		} else if(hitbox == Hitbox.POLYGON || entity.hitbox == Hitbox.POLYGON){
			if(poly == null || entity.poly == null)
				throw new RuntimeException("Both subjects must have a polygon to perform a polygon collision!");
			
			return false;//TODO:
		} else if(hitbox == Hitbox.PIXEL || entity.hitbox == Hitbox.PIXEL){
			if(rotated1 || rotated2)
				throw new RuntimeException("Rotated elements with pixel perfect hitbox is not supported for collision detection.");
			
			//Freeze images
			
			Image2D img1 = nextImage();
			Image2D img2 = entity.nextImage();
			
			if(!img1.hasPixelData() || !img2.hasPixelData())
				throw new RuntimeException("No pixeldata found for one or more of the entities.");
			
			return false;//TODO:
		}
		
		throw new IllegalStateException("No proper collision handling methods found.");
	}
	
	public Vector2 getCenterCord(){
		return new Vector2(bounds.x + bounds.width / 2, bounds.y + bounds.height / 2);
	}
	
	public Vector2 getPos(){
		return new Vector2(bounds.x, bounds.y);
	}
	
	public void setPolygon(float[] vertices){
		poly = new Polygon(vertices);
	}
	
	public Image2D nextImage(){
		return null;
	}
	
	void runEvents(){
		for(Event event : deleteEvents)
			events.remove(event);
		
		deleteEvents.clear();

		for(Event event : events)
			event.eventHandling();
	}
}
