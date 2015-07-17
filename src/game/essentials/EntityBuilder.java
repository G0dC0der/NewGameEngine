package game.essentials;

import game.core.Entity;

public class EntityBuilder {

	private Entity entity;
	
	public EntityBuilder(){
		entity = new Entity();
	}
	
	public EntityBuilder zIndex(int zIndex){
		entity.zIndex(zIndex);
		return this;
	}
	
	public EntityBuilder image(Image2D... image){
		entity.setImage(image);
		return this;
	}
	
	public EntityBuilder image(int speed, Image2D... image){
		entity.setImage(speed, image);
		return this;
	}
	
	public EntityBuilder image(Animation<Image2D> image){
		entity.setImage(image);
		return this;
	}
	
	public EntityBuilder x(float x){
		entity.bounds.x = x;
		return this;
	}
	
	public EntityBuilder y(float y){
		entity.bounds.y = y;
		return this;
	}
	
	public EntityBuilder width(float width){
		entity.bounds.width = width;
		return this;
	}
	
	public EntityBuilder height(float heigh){
		entity.bounds.height = heigh;
		return this;
	}
	
	public EntityBuilder offsetX(float offsetX){
		entity.offsetX = offsetX;
		return this;
	}
	
	public EntityBuilder offsetY(float offsetY){
		entity.offsetY = offsetY;
		return this;
	}
	
	public EntityBuilder alpha(float alpha){
		entity.alpha = alpha;
		return this;
	}
	
	public EntityBuilder rotation(float rotation){
		entity.setRotation(rotation);
		return this;
	}
	
	public Entity build(){
		return entity;
	}
}
