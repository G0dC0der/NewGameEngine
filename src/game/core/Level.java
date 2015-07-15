package game.core;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import game.essentials.Entry;
import game.events.Event;
import game.events.TaskEvent;

public abstract class Level {
	
	public static final byte HOLLOW = 0;
	public static final byte SOLID = 0;
	
	static final Comparator<Object> Z_INDEX_SORT = (obj1, obj2) ->  {
		if(obj1 instanceof Entity && obj2 instanceof Entity){
			Entity e1 = (Entity) obj1;
			Entity e2 = (Entity) obj2;
			
			return e1.getZIndex() - e2.getZIndex();
		}
		return 0;
	};

	protected Engine engine;
	private List<Object> gameObjects;
	private List<Entry<Integer, Object>> awatingObjects;
	private List<Entry<Integer, Object>> deleteObjects;
	boolean sort;
	
	abstract byte get(int x, int y);
	
	abstract boolean isSolid(int x, int y);

	abstract boolean isHollow(int x, int y);
	
	abstract void init();
	
	abstract void build();
	
	abstract void dispose();
	
	void gameLoop(){
		insertDelete();
		
		if(sort){
			Collections.sort(gameObjects, Z_INDEX_SORT);
			sort = false;
		}
		
		update();
	}
	
	public void add(Entity entity){
		awatingObjects.add(new Entry<Integer, Object>(0, entity));
	}
	
	public void addAfter(Entity entity, int framesDelay){
		awatingObjects.add(new Entry<Integer, Object>(framesDelay, entity));
	}
	
	public void addWhen(Entity entity, TaskEvent addEvent){
		Event wrapper = ()->{
			if(addEvent.eventHandling()){
				add(entity);
				discard(this);
			}
		};
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
		addAfter(event, 0);		
	}
	
	public void addAfter(Event event, int framesDelay){
		awatingObjects.add(new Entry<Integer, Object>(framesDelay, event));
	}
	
	public void addWhen(Event event, TaskEvent addEvent){
		Event wrapper = ()->{
			if(addEvent.eventHandling()){
				add(event);
				discard(this);
			}
		};
		add(wrapper);
	}
	
	public void addTemp(Event event, int lifeFrames){
		add(event);
		discardAfter(event, lifeFrames);
	}
	
	public void addTemp(Event event, TaskEvent discardEvent){
		add(event);
		discardWhen(event, discardEvent);
	}
	
	public void addShort(Event event){
		add(()->{
			event.eventHandling();
			discard(this);
		});
	}
	
	public void discard(Object object){
		discardAfter(object, 0);
	}
	
	public void discardAfter(Object object, int framesDelay){
		deleteObjects.add(new Entry<Integer, Object>(framesDelay, object));
	}
	
	public void discardWhen(Object object, TaskEvent discardEvent){
		Event wrapper = ()->{
			if(discardEvent.eventHandling()){
				discard(this);
				discard(object);
			}
		};
		add(wrapper);
	}
	
	private void update(){
		for(Object object : gameObjects){
			if(object instanceof PlayableEntity){
				
			} else if(object instanceof MobileEntity){
				MobileEntity entity = (MobileEntity) object;
				
			} else if(object instanceof Entity){
				Entity entity = (Entity) object;
				entity.runEvent();
			}
		}
	}
	
	private void insertDelete(){
		for(int i = 0; i < awatingObjects.size(); i++){
			Entry<Integer, Object> entry = awatingObjects.get(i);
			
			if(entry.key-- <= 0){
				gameObjects.add(entry.value);
				awatingObjects.remove(i);
				sort = true;
				i--;
				
				if(entry.value instanceof Entity){
					Entity entity = (Entity)entry.value;
					entity.level = this;
					entity.engine = engine;
					entity.init();
				}
			}
		}
		
		for(int i = 0; i < deleteObjects.size(); i++){
			Entry<Integer, Object> entry = deleteObjects.get(i);
			
			if(entry.key-- <= 0){
				gameObjects.remove(entry.value);
				deleteObjects.remove(i);
				i--;
				
				if(entry.value instanceof Entity){
					Entity entity = (Entity)entry.value;
					entity.dispose();
				}
			}
		}
	}
}
