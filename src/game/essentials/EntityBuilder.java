package game.essentials;

import game.core.Entity;
import game.events.Event;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class EntityBuilder {

	private int zIndex;
	private Animation<Image2D> image;
	private Hitbox hitbox;
	private float x, y, width, height, offsetX, offsetY, alpha, rotation;
	private List<Event> events;
	
	public EntityBuilder() {
		events = new ArrayList<>();
		alpha = 1;
		hitbox = Hitbox.RECTANGLE;
	}
	
	public EntityBuilder zIndex(int zIndex){
		this.zIndex = zIndex;
		return this;
	}
	
	public EntityBuilder image(Image2D... image){
		image(3, image);
		return this;
	}
	
	public EntityBuilder image(int speed, Image2D... image){
		image(new Animation<>(3, image));
		return this;
	}
	
	public EntityBuilder image(Animation<Image2D> image){
		this.image = image;
		return this;
	}
	
	public EntityBuilder x(float x){
		this.x = x;
		return this;
	}
	
	public EntityBuilder move(float x, float y){
		this.x = x;
		this.y = y;
		return this;
	}
	
	public EntityBuilder y(float y){
		this.y = y;
		return this;
	}
	
	public EntityBuilder width(float width){
		this.width = width;
		return this;
	}
	
	public EntityBuilder height(float heigh){
		this.height = heigh;
		return this;
	}
	
	public EntityBuilder offsetX(float offsetX){
		this.offsetX = offsetX;
		return this;
	}
	
	public EntityBuilder offsetY(float offsetY){
		this.offsetY = offsetY;
		return this;
	}
	
	public EntityBuilder alpha(float alpha){
		this.alpha = alpha;
		return this;
	}
	
	public EntityBuilder rotation(float rotation){
		this.rotation = rotation;
		return this;
	}
	
	public EntityBuilder events(Event... events){
		this.events.addAll(Arrays.asList(events));
		return this;
	}
	
	public EntityBuilder hitbox(Hitbox hitbox){
		this.hitbox = hitbox;
		return this;
	}

	public Entity build(){
		Entity entity = new Entity();
		pasteData(entity);
		return entity;
	}
	
	public <T extends Entity> T build(Class<T> clazz, Object... args){
		T entity;
		try{
			if(args.length == 0)
				entity = clazz.newInstance();
			else{
				@SuppressWarnings("rawtypes")
				Class[] clazzez = (Class[]) Arrays.stream(args).map(obj -> obj.getClass()).collect(Collectors.toList()).toArray();
				entity = clazz.getDeclaredConstructor(clazzez).newInstance(args);
			}
		}catch(Exception e){
			throw new RuntimeException(e);
		}
		
		pasteData(entity);
		return entity;
	}
	
	private void pasteData(Entity dest){
		dest.bounds.pos.x = x;
		dest.bounds.pos.y = y;
		dest.bounds.size.width = width;
		dest.bounds.size.height = height;
		dest.offsetX = offsetX;
		dest.offsetY = offsetY;
		dest.alpha = alpha;
		dest.setRotation(rotation);
		dest.zIndex(zIndex);
		dest.setHitbox(hitbox);
		dest.id = "Entity Builder";
		if(image != null)
			dest.setImage(image);
		
		for(Event event : events)
			dest.addEvent(event);
	}
}