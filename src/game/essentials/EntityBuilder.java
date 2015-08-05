package game.essentials;

import game.core.Entity;
import game.events.Event;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EntityBuilder {

	protected int zIndex;
	protected Animation<Image2D> image;
	protected float x, y, width, height, offsetX, offsetY, alpha, rotation;
	protected List<Event> events;
	
	public EntityBuilder() {
		events = new ArrayList<>();
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
	
	public Entity build(){
		Entity entity = new Entity();
		entity.bounds.x = x;
		entity.bounds.y = y;
		entity.bounds.width = width;
		entity.bounds.height = height;
		entity.offsetX = offsetX;
		entity.offsetY = offsetY;
		entity.alpha = alpha;
		entity.setRotation(rotation);

		for(Event event : events)
			entity.addEvent(event);
			
		return entity;
	}
}
