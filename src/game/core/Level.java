package game.core;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import game.essentials.Entry;
import game.essentials.Utils;
import game.events.Event;
import game.events.TaskEvent;

public abstract class Level {
	
	public static final byte HOLLOW = 0;
	public static final byte SOLID = 0;
	
	static final Comparator<Entity> Z_INDEX_SORT = (obj1, obj2) ->  obj1.getZIndex() - obj2.getZIndex();

	protected Engine engine;
	private List<Entry<Integer, Entity>> awatingObjects, deleteObjects;
	List<Entity> gameObjects;
	boolean sort;
	
	public abstract byte get(int x, int y);
	
	public abstract boolean isSolid(int x, int y);

	public abstract boolean isHollow(int x, int y);
	
	public abstract void init();
	
	public abstract void build();
	
	public abstract void dispose();
	
	void gameLoop(){
		insertDelete();
		
		if(sort){
			Collections.sort(gameObjects, Z_INDEX_SORT);
			sort = false;
		}
		
		update();
	}
	
	public void add(Entity entity){
		awatingObjects.add(new Entry<Integer, Entity>(0, entity));
	}
	
	public void addAfter(Entity entity, int framesDelay){
		awatingObjects.add(new Entry<Integer, Entity>(framesDelay, entity));
	}
	
	public void addWhen(Entity entity, TaskEvent addEvent){
		Entity wrapper = new Entity();
		wrapper.addEvent(()->{
			if(addEvent.eventHandling()){
				wrapper.getLevel().add(entity);
				wrapper.getLevel().discard(wrapper);
			}
		});
		
		add(wrapper);
	}
	
	public void addTemp(Entity entity, int lifeFrames){
		add(entity);
		discardAfter(entity, lifeFrames);
	}
	
	public void addTemp(Entity entity, TaskEvent discardEvent){
		add(entity);
		discardWhen(entity, discardEvent);
	}
	
	public void add(Event event){
		addAfter(Utils.wrap(event), 0);		
	}
	
	public void addAfter(Event event, int framesDelay){
		addAfter(Utils.wrap(event), framesDelay);
	}
	
	public void addWhen(Event event, TaskEvent addEvent){
		addWhen(Utils.wrap(event), addEvent);
	}
	
	public void addTemp(Event event, int lifeFrames){
		Entity wrapper = Utils.wrap(event);
		add(wrapper);
		discardAfter(wrapper, lifeFrames);
	}
	
	public void addTemp(Event event, TaskEvent discardEvent){
		Entity wrapper = Utils.wrap(event);
		add(wrapper);
		discardWhen(wrapper, discardEvent);
	}
	
	public void addShort(Event event){
		Entity wrapper = Utils.wrap(event);
		wrapper.addEvent(()->{
			event.eventHandling();
			wrapper.getLevel().discard(wrapper);
		});
	}
	
	public void discard(Entity entity){
		discardAfter(entity, 0);
	}
	
	public void discardAfter(Entity entity, int framesDelay){
		deleteObjects.add(new Entry<Integer, Entity>(framesDelay, entity));
	}
	
	public void discardWhen(Entity entity, TaskEvent discardEvent){
		Entity wrapper = new Entity();
		wrapper.addEvent(()->{
			if(discardEvent.eventHandling()){
				discard(entity);
				discard(wrapper);
			}
		});
		
		add(wrapper);
	}
	
	public Entity findWrapper(Event event){
		for(int i = 0; i < gameObjects.size(); i++){
			Entity entity = gameObjects.get(i);
			
			if(entity.id.equals("WRAPPER") && entity.events.contains(event))
				return entity;
		}
		
		return null;
	}
	
	public List<PlayableEntity> getMainCharacters(){
		return null;
	}
	
	public List<PlayableEntity> getAliveMainCharacters(){
		return null;
	}
	
	private void update(){
		for(Entity entity : gameObjects){
			if(entity instanceof MobileEntity){
				
			} else if(entity instanceof PlayableEntity){
				
			} else {
				entity.runEvents();
			}
		}
	}
	
	private void insertDelete(){
		for(int i = 0; i < awatingObjects.size(); i++){
			Entry<Integer, Entity> entry = awatingObjects.get(i);
			
			if(entry.key-- <= 0){
				gameObjects.add(entry.value);
				awatingObjects.remove(i);
				sort = true;
				i--;
				entry.value.level = this;
				entry.value.engine = engine;
				entry.value.init();
			}
		}
		
		for(int i = 0; i < deleteObjects.size(); i++){
			Entry<Integer, Entity> entry = deleteObjects.get(i);
			
			if(entry.key-- <= 0){
				gameObjects.remove(entry.value);
				deleteObjects.remove(i);
				i--;
				entry.value.dispose();
			}
		}
	}
}
