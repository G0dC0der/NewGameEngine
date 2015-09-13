package pojahn.game.core;

import java.util.ArrayList;
import java.util.List;

import static pojahn.game.core.Collisions.*;
import pojahn.game.essentials.Animation;
import pojahn.game.essentials.Bounds;
import pojahn.game.essentials.Hitbox;
import pojahn.game.essentials.Image2D;
import pojahn.game.essentials.SoundEmitter;
import pojahn.game.events.CloneEvent;
import pojahn.game.events.Event;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;

public class Entity{

	public final Bounds bounds;
	public SoundEmitter sounds;
	public String id;
	public float alpha, offsetX, offsetY;
	public boolean flipX, flipY;
	
	protected CloneEvent cloneEvent;
	
	Level level;
	Engine engine;
	List<Event> events, deleteEvents;
	Polygon poly;
	boolean present;
	
	private Animation<Image2D> image;
	private Entity originator;
	private Hitbox hitbox;
	private boolean quickCollision, visible, active;
	private int zIndex;
	
	public Entity(){
		bounds = new Bounds();
		sounds = new SoundEmitter(this);
		alpha = offsetX = offsetY = 1;
		active = true;
		events = new ArrayList<>();
		hitbox = Hitbox.RECTANGLE;
		deleteEvents = new ArrayList<>();
	}
	
	public Entity getClone(){
		Entity clone = new Entity();
		copyData(clone);
		if(cloneEvent != null)
			cloneEvent.handleClonded(clone);
		
		return clone;
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
		bounds.size.width  = image.getArray()[0].getWidth();
		bounds.size.height = image.getArray()[0].getHeight();
	}
	
	public Animation<Image2D> getImage(){
		return image;
	}
	
	public Entity move(float x, float y){
		bounds.pos.x = x;
		bounds.pos.y = y;
		return this;
	}

	public Entity center(Entity target){
		bounds.pos.x = target.centerX() - (width() / 2);
		bounds.pos.y = target.centerY() - (height() / 2);
		return this;
	}
	
	public void nudge(float relX, float relY){
		bounds.pos.x += relX;
		bounds.pos.y += relY;
	}
	
	public void init() {}
	
	public void dispose() {}
	
	public void render(SpriteBatch batch){
		if(!visible || alpha == 0)
			return;
		
		Color defColor = batch.getColor();
		batch.setColor(new Color(defColor.r, defColor.g, defColor.b, alpha));
		basicRender(batch);
		batch.setColor(defColor);
	}
	
	public Engine getEngine(){
		return engine;
	}
	
	public final void zIndex(int zIndex){
		this.zIndex = zIndex;
		if(level != null)
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
		deleteEvents.add(event);
	}
	
	public boolean collidesWith(Entity entity){
		boolean rotated1 = bounds.rotation != 0 && !quickCollision;
		boolean rotated2 = entity.bounds.rotation != 0 && !entity.quickCollision;
		
		if(hitbox == Hitbox.NONE || entity.hitbox == Hitbox.NONE){
			return false;
		} else if(hitbox == Hitbox.RECTANGLE && entity.hitbox == Hitbox.RECTANGLE){
			return (rotated1 || rotated2) ? rotatedRectanglesCollide(bounds, entity.bounds) : rectanglesCollide(bounds.toRectangle(), entity.bounds.toRectangle());
		} else if((hitbox == Hitbox.RECTANGLE && entity.hitbox == Hitbox.CIRCLE) || (hitbox == Hitbox.CIRCLE && entity.hitbox == Hitbox.RECTANGLE)){
			Entity rectangle 	= hitbox == Hitbox.RECTANGLE 	? this : entity;
			Entity circle 		= hitbox == Hitbox.CIRCLE 		? this : entity;
			
			if(rectangle.getRotation() != 0)
				throw new RuntimeException("No collision method for rotated rectangle vs circle.");
			
			return circleRectangleCollide(circle.bounds.toCircle(), rectangle.bounds.toRectangle());
		} else if(hitbox == Hitbox.CIRCLE && entity.hitbox == Hitbox.CIRCLE){
			return circleVsCircle(bounds.toCircle(), entity.bounds.toCircle());
		} else if(hitbox == Hitbox.POLYGON || entity.hitbox == Hitbox.POLYGON){
			//return polygonsCollide(this, entity);
		} else if(hitbox == Hitbox.PIXEL || entity.hitbox == Hitbox.PIXEL){
			if(bounds.rotation != 0 || entity.bounds.rotation != 0)	
				return rectanglesCollide(getBoundingBox(bounds), getBoundingBox(entity.bounds)) && pixelPerfectRotation(buildMatrix(this),	getImage().getCurrentObject(),
																														buildMatrix(entity), entity.getImage().getCurrentObject());
			
			return rectanglesCollide(bounds.toRectangle(), entity.bounds.toRectangle()) && pixelPerfect(bounds.toRectangle(), getImage().getCurrentObject(), flipX, flipY,
																										entity.bounds.toRectangle(), entity.getImage().getCurrentObject(), entity.flipX, entity.flipY);
		}
		
		throw new IllegalStateException("No proper collision handling methods found.");
	}
	
	public Vector2 getCenterCord(){
		return bounds.center();
	}
	
	public Vector2 getPos(){
		return bounds.pos.cpy();
	}
	
	public void setPolygon(float[] vertices){
		poly = new Polygon(vertices);
	}
	
	public float getRotation(){
		return bounds.rotation;
	}

	public void setRotation(float rotation){
		bounds.rotation = rotation;
	}
	
	public void rotate(float amount){
		bounds.rotation += amount;
	}
	
	public float x(){
		return bounds.pos.x;
	}
	
	public float y(){
		return bounds.pos.y;
	}
	
	public float width(){
		return bounds.size.width;
	}
	
	public float height(){
		return bounds.size.height;
	}
	
	public float halfWidth(){
		return bounds.size.width / 2;
	}
	
	public float halfHeight(){
		return bounds.size.height / 2;
	}
	
	public float centerX(){
		return bounds.pos.x + bounds.size.width / 2;
	}
	
	public float centerY(){
		return bounds.pos.y + bounds.size.height / 2;
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
		bounds.pos.x--;
		bounds.pos.y--;
		bounds.size.width += 2;
		bounds.size.height += 2;
	}
	
	public void contract(){
		bounds.pos.x++;
		bounds.pos.y++;
		bounds.size.width -= 2;
		bounds.size.height -= 2;
	}
	
	public boolean isPresent(){
		return present;
	}
	
	public Image2D nextImage(){
		return visible && image != null ? image.getObject() : null;
	}
	
	public boolean isCloneOf(Entity originator){
		return this.originator == originator;
	}
	
	public void setCloneEvent(CloneEvent cloneEvent){
		this.cloneEvent = cloneEvent;
	}
	
	public boolean canSee(Entity target){
		return  !Collisions.solidSpace(x(), centerY(), target.x(), target.centerY(), getLevel()) ||
				!Collisions.solidSpace(centerX(), centerY(), target.centerX(), target.centerY(), getLevel()) ||
				!Collisions.solidSpace(x() + width(), centerY(), target.x() + target.width(), target.centerY(), getLevel());
	}
	
	public Vector2 getFrontPosition(){
		float locX = bounds.pos.x + width() / 2;
		float locY = bounds.pos.y + height() / 2;
		
		locX += Math.cos(Math.toRadians(getRotation())) * (width() / 2);
		locY += Math.sin(Math.toRadians(getRotation())) * (height() / 2);
		
		return new Vector2(locX,locY);
	}
	
	public Vector2 getRarePosition(){
		float locX = bounds.pos.x + width() / 2;
		float locY = bounds.pos.y + height() / 2;
		
		locX -= Math.cos(Math.toRadians(getRotation())) * (width() / 2);
		locY -= Math.sin(Math.toRadians(getRotation())) * (height() / 2);
		
		return new Vector2(locX,locY);
	}
	
	protected void copyData(Entity clone){
		clone.originator = this;
		clone.bounds.pos.x = bounds.pos.x;
		clone.bounds.pos.y = bounds.pos.y;
		clone.bounds.size.width = bounds.size.width;
		clone.bounds.size.height = bounds.size.height;
		clone.active = active;
		clone.visible = visible;
		clone.alpha = alpha;
		clone.bounds.rotation = bounds.rotation;
		clone.zIndex = zIndex;
		clone.hitbox = hitbox;
		clone.quickCollision = quickCollision;
		clone.offsetX = offsetX;
		clone.offsetY = offsetY;
		clone.flipX = flipX;
		clone.flipY = flipY;
		clone.sounds.maxDistance = sounds.maxDistance;
		clone.sounds.maxVolume = sounds.maxVolume;
		clone.sounds.power = sounds.power;
		clone.sounds.useFalloff = sounds.useFalloff;
		if(image != null)
			clone.image = image.getClone();
		if(poly != null)
			clone.poly = new Polygon(poly.getTransformedVertices());
	}
	
	protected void basicRender(SpriteBatch batch){
		batch.draw(	nextImage(), 
					bounds.pos.x + offsetX, 
					bounds.pos.y + offsetY, 
					centerX() - (bounds.pos.x + offsetX), 
					centerY() - (bounds.pos.y + offsetY), 
					bounds.size.width, 
					bounds.size.height, 
					1, 
					1, 
					bounds.rotation, 
					0, 
					0, 
					(int)bounds.size.width, 
					(int)bounds.size.height,
					flipX, 
					!flipY);
	}
	
	void runEvents(){
		events.removeAll(deleteEvents);
		deleteEvents.clear();

		for(Event event : events)
			event.eventHandling();
	}
}
