package game.core;

import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import game.essentials.Animation;
import game.essentials.Hitbox;
import game.essentials.Image2D;
import game.events.Event;

public class Entity {

	public final Rectangle bounds;
	public String id;
	public int alpha, scaleX, scaleY, offsetX, offsetY;
	public boolean flipX, flipY;
	
	Level level;
	Engine engine;
	List<Event> events, deleteEvents;
	Polygon poly;
	
	private Animation<Image2D> image;
	private Hitbox hitbox;
	private boolean quickCollision;
	private float rotation;
	private int zIndex;
	
	public Entity(){
		bounds = new Rectangle();
		offsetX = offsetY = 1;
	}
	
	public void init() {}
	
	public void dispose() {}
	
	public void render(SpriteBatch batch){
		Vector2 center = getCenterCord();
		Color defColor = batch.getColor();
		Color newColor = new Color(defColor);
		newColor.a = alpha;
		
		batch.setColor(newColor);
		batch.draw(nextImage(), bounds.x + offsetX, 
								bounds.y + offsetY, 
								center.x, 
								center.y, 
								bounds.width, 
								bounds.height, 
								scaleX, 
								scaleY, 
								rotation, 
								0, 
								0, 
								(int)bounds.width, 
								(int)bounds.height,
								flipX, 
								flipY);
		batch.setColor(defColor);
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
				return Collisions.rotatedRectanglesCollide(this, entity);
			else
				return Collisions.rectanglesCollide(this, entity);
		} else if((hitbox == Hitbox.RECTANGLE && entity.hitbox == Hitbox.CIRCLE) || (hitbox == Hitbox.CIRCLE && entity.hitbox == Hitbox.RECTANGLE)){
			Entity rectangle 	= hitbox == Hitbox.RECTANGLE 	? this : entity;
			Entity circle 		= hitbox == Hitbox.CIRCLE 		? this : entity;
			
			if(rectangle.rotation != 0 && !rectangle.quickCollision)
				return false;//TODO
			else
				return Collisions.circleRectangleCollide(circle, rectangle);
		} else if(hitbox == Hitbox.CIRCLE && entity.hitbox == Hitbox.CIRCLE){
			return Collisions.circleVsCircle(this, entity);
		} else if(hitbox == Hitbox.POLYGON || entity.hitbox == Hitbox.POLYGON){
			if(poly == null || entity.poly == null)
				throw new RuntimeException("Both subjects must have a polygon to perform a polygon collision!");
			
			return false;//TODO:
		} else if(hitbox == Hitbox.PIXEL || entity.hitbox == Hitbox.PIXEL){
			if(rotated1 || rotated2)
				throw new RuntimeException("Rotated elements with pixel perfect hitbox is not supported for collision detection.");
			
			return Collisions.pixelPerfect(this, entity);
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
	
	public float getRotation(){
		return rotation;
	}
	
	public void setRotation(float rotation){
		this.rotation = rotation;
	}
	
	public void rotate(float amount){
		rotation += amount;
	}
	
	public float x(){
		return bounds.x;
	}
	
	public float y(){
		return bounds.y;
	}
	
	public float width(){
		return bounds.width;
	}
	
	public float height(){
		return bounds.height;
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
