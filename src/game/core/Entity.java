package game.core;

import java.util.List;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import game.events.Event;

public class Entity {
	
	public final Vector2 pos;
	public final Rectangle size;
	
	Level level;
	Engine engine;
	private int zIndex;
	private List<Event> events, deleteEvents;
	
	public Entity(){
		pos = new Vector2();
		size = new Rectangle();
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
	
	void runEvent(){
		for(Event event : deleteEvents)
			events.remove(event);
		
		deleteEvents.clear();

		for(Event event : events)
			event.eventHandling();
	}
}
