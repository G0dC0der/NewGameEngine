package game.core;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import game.essentials.Animation;
import game.essentials.Hitbox;
import game.essentials.Image2D;
import game.essentials.SoundEmitter;
import game.events.CloneEvent;
import game.events.Event;

public class Entity{

	public final Rectangle bounds;
	public final SoundEmitter sounds;
	public String id;
	public float alpha, scaleX, scaleY, offsetX, offsetY;
	public boolean flipX, flipY;
	
	protected CloneEvent cloneEvent;
	
	Level level;
	Engine engine;
	List<Event> events, deleteEvents;
	Polygon poly;
	
	private Animation<Image2D> image;
	private Entity mother;
	private Hitbox hitbox;
	private boolean quickCollision, visible, active;
	private float rotation;
	private int zIndex;
	
	public Entity(){
		bounds = new Rectangle();
		sounds = new SoundEmitter(this);
		alpha = scaleX = scaleY = offsetX = offsetY = 1;
		events = new ArrayList<>();
		deleteEvents = new ArrayList<>();
	}
	
	public Entity(Entity src){
		this();
		copyData(src);
		if(src.cloneEvent != null)
			src.cloneEvent.handleClonded(this);
	}
	
	public void setImage(Image2D... images){
		setImage(3, images);
	}
	
	public void setImage(int speed, Image2D... images){
		setImage(new Animation<>(speed, images));
	}
	
	public void setImage(Animation<Image2D> image){
		this.image = image;
		
		visible = true;
		bounds.width  = image.getArray()[0].getWidth();
		bounds.height = image.getArray()[0].getHeight();
	}
	
	public Animation<Image2D> getImage(){
		return image;
	}
	
	public Entity move(float x, float y){
		bounds.x = x;
		bounds.y = y;
		return this;
	}
	
	public void init() {}
	
	public void dispose() {}
	
	public void render(SpriteBatch batch){
		if(!visible || alpha == 0)
			return;
		
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
	
	public Engine getEngine(){
		return engine;
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
	
	public boolean isVisible(){
		return visible;
	}
	
	public void setVisible(boolean visible){
		this.visible = visible;
	}
	
	public boolean isActive(){
		return active;
	}
	
	public void activate(boolean activate){
		this.active = activate;
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
				throw new RuntimeException("No collision method for rotated rectangle vs circle.");//TODO
			else
				return Collisions.circleRectangleCollide(circle, rectangle);
		} else if(hitbox == Hitbox.CIRCLE && entity.hitbox == Hitbox.CIRCLE){
			return Collisions.circleVsCircle(this, entity);
		} else if(hitbox == Hitbox.POLYGON || entity.hitbox == Hitbox.POLYGON){
			return Collisions.polygonsCollide(this, entity);
		} else if(hitbox == Hitbox.PIXEL || entity.hitbox == Hitbox.PIXEL){
			return Collisions.rectanglesCollide(this, entity) && Collisions.pixelPerfect(this, entity);
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
	
	public float halfWidth(){
		return bounds.width / 2;
	}
	
	public float halfHeight(){
		return bounds.height / 2;
	}
	
	public float centerX(){
		return bounds.x + bounds.width / 2;
	}
	
	public float centerY(){
		return bounds.y + bounds.height / 2;
	}
	
	public Hitbox getHitbox() {
		return hitbox;
	}

	public void setHitbox(Hitbox hitbox) {
		this.hitbox = hitbox;
	}

	public float dist(Entity entity){
		return (float) Collisions.distance(x(), y(), entity.x(), entity.y());
	}
	
	public void expand(){
		bounds.x--;
		bounds.y--;
		bounds.width += 2;
		bounds.height += 2;
	}
	
	public void contract(){
		bounds.x++;
		bounds.y++;
		bounds.width -= 2;
		bounds.height -= 2;
	}
	
	public Image2D nextImage(){
		return visible && image != null ? image.getObject() : null;
	}
	
	public boolean isCloneOf(Entity parent){
		return mother == parent;
	}
	
	public void setCloneEvent(CloneEvent cloneEvent){
		this.cloneEvent = cloneEvent;
	}
	
	protected void copyData(Entity src){
		mother = src;
		bounds.x = src.bounds.x;
		bounds.y = src.bounds.y;
		bounds.width = src.bounds.width;
		active = src.active;
		visible = src.visible;
		alpha = src.alpha;
		rotation = src.rotation;
		zIndex = src.zIndex;
		hitbox = src.hitbox;
		quickCollision = src.quickCollision;
		offsetX = src.offsetX;
		offsetY = src.offsetY;
		flipX = src.flipX;
		flipY = src.flipY;
		scaleX = src.scaleX;
		scaleY = src.scaleY;
		sounds.maxDistance = src.sounds.maxDistance;
		sounds.maxVolume = src.sounds.maxVolume;
		sounds.power = src.sounds.power;
		sounds.useFalloff = src.sounds.useFalloff;
		if(src.image != null)
			image = src.image.getClone();
		if(src.poly != null)
			poly = new Polygon(src.poly.getTransformedVertices());
	}
	
	void runEvents(){
		events.removeAll(deleteEvents);
		deleteEvents.clear();

		for(Event event : events)
			event.eventHandling();
	}
}
