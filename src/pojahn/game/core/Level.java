package pojahn.game.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import pojahn.game.essentials.GameState;
import pojahn.game.essentials.Keystrokes;
import pojahn.game.essentials.Utils;
import pojahn.game.essentials.Vitality;
import pojahn.game.events.Event;
import pojahn.game.events.TaskEvent;
import pojahn.lang.Entry;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.math.Vector2;

public abstract class Level {
	
	public enum Tile{
		SOLID,
		HOLLOW,
		GOAL,
		LETHAL,
		CUSTOM_1,
		CUSTOM_2,
		CUSTOM_3,
		CUSTOM_4,
		CUSTOM_5,
		CUSTOM_6,
		CUSTOM_7,
		CUSTOM_8,
		CUSTOM_9,
		CUSTOM_10
	}
	
	static final Comparator<Entity> Z_INDEX_SORT = (obj1, obj2) ->  obj1.getZIndex() - obj2.getZIndex();

	Engine engine;

	private List<Entry<Integer, Entity>> awatingObjects, deleteObjects;
	private List<PlayableEntity> mainCharacters;
	
	List<Entity> gameObjects, soundListeners;
	boolean sort;
	
	protected Level(){
		awatingObjects = new LinkedList<>();
		deleteObjects = new LinkedList<>();
		soundListeners = new ArrayList<>();
		gameObjects = new LinkedList<>();
		mainCharacters = new ArrayList<>();
	}
	
	public abstract int getWidth();

	public abstract int getHeight();
	
	public abstract Tile tileAt(int x, int y);
	
	public abstract void setTileOnLayer(int x, int y, Tile tile);
	
	public abstract void removeTileOnLayer(int x, int y);
	
	public abstract void clearTileLayer();
	
	public abstract boolean isSolid(int x, int y);

	public abstract boolean isHollow(int x, int y);
	
	public abstract void init() throws Exception;
	
	public abstract void build();
	
	public abstract void dispose();
	
	public Tile tileAt(Vector2 cord){
		return tileAt((int)cord.x, (int)cord.y);
	}
	
	public void processMeta(Serializable meta) {}

	public Serializable getMeta(){
		return null;
	}
	
	public boolean cpPresent(){
		return false;
	}
	
	public Music getStageMusic(){
		return null;
	}
	
	public Engine getEngine(){
		return engine;
	}
	
	public boolean outOfBounds(float targetX, float targetY){
		if(targetX >= getWidth() ||
		   targetY >= getHeight() || 
		   targetX < 0 ||
		   targetY < 0)
			return true;
		
		return false;
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
	
	public void temp(Entity entity, int lifeFrames){
		add(entity);
		discardAfter(entity, lifeFrames);
	}
	
	public void temp(Entity entity, TaskEvent discardEvent){
		add(entity);
		discardWhen(entity, discardEvent);
	}
	
	public Entity add(Event event){
		Entity wrapper = Utils.wrap(event);
		addAfter(wrapper, 0);		
		
		return wrapper;
	}
	
	public Entity addAfter(Event event, int framesDelay){
		Entity wrapper = Utils.wrap(event);
		addAfter(wrapper, framesDelay);
		
		return wrapper;
	}
	
	public Entity addWhen(Event event, TaskEvent addEvent){
		Entity wrapper = Utils.wrap(event);
		addWhen(wrapper, addEvent);
		
		return wrapper;
	}
	
	public Entity temp(Event event, int lifeFrames){
		Entity wrapper = Utils.wrap(event);
		add(wrapper);
		discardAfter(wrapper, lifeFrames);
		
		return wrapper;
	}
	
	public Entity temp(Event event, TaskEvent discardEvent){
		Entity wrapper = Utils.wrap(event);
		add(wrapper);
		discardWhen(wrapper, discardEvent);
		
		return wrapper;
	}
	
	public Entity runOnceWhen(Event event, TaskEvent whenToRun){
		Entity wrapper = new Entity();
		wrapper.addEvent(()->{
			if(whenToRun.eventHandling()){
				event.eventHandling();
				wrapper.getLevel().discard(wrapper);
			}
		});
		add(wrapper);
		
		return wrapper;
	}
	
	public Entity runOnceAfter(Event event, int framesDelay){
		int[] counter = {0};
		
		Entity wrapper = new Entity();
		wrapper.addEvent(()->{
			if(counter[0]++ > framesDelay){
				event.eventHandling();
				discard(wrapper);
			}
		});
		add(wrapper);
		
		return wrapper;
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
	
	public List<PlayableEntity> getMainCharacters(){
		return mainCharacters;
	}
	
	public List<PlayableEntity> getNonDeadMainCharacters(){
		return mainCharacters.stream()
				.filter(el -> el.getState() == Vitality.ALIVE || el.getState() == Vitality.COMPLETED)
				.collect(Collectors.toList());
	}
	
	public List<PlayableEntity> getAliveMainCharacters(){
		return mainCharacters.stream()
				.filter(el -> el.getState() == Vitality.ALIVE)
				.collect(Collectors.toList());
	}
	
	public List<? extends Entity> getSoundListeners(){
		return soundListeners.isEmpty() ? getNonDeadMainCharacters() : soundListeners;
	}
	
	public void addSoundListener(Entity listener){
		soundListeners.add(listener);
	}
	
	protected void clean(){
		awatingObjects.clear();
		deleteObjects.clear();
		gameObjects.forEach(e -> e.dispose());
		gameObjects.clear();
		clearTileLayer();
		mainCharacters.clear();
	}

	void gameLoop(){
		insertDelete();
		
		if(sort){
			Collections.sort(gameObjects, Z_INDEX_SORT);
			sort = false;
		}
		
		updateEntities();
	}
	
	private void updateEntities(){
		for(Entity entity : gameObjects){
			if(!entity.isActive())
				continue;
			
			if(entity instanceof PlayableEntity){
				PlayableEntity play = (PlayableEntity) entity;
				Keystrokes buttonsDown;
				
				if(play.isGhost())
					buttonsDown = play.nextReplayFrame();
				else if(engine.getGameState() == GameState.ACTIVE && play.getState() == Vitality.ALIVE)
					buttonsDown = engine.playingReplay() ? engine.getReplayFrame(play) : PlayableEntity.checkButtons(play.getController());
				else
					buttonsDown = PlayableEntity.STILL;

				if(play.getState() == Vitality.ALIVE && engine.getGameState() == GameState.ACTIVE && !engine.playingReplay())
					engine.registerReplayFrame(play, buttonsDown);
				
				if(buttonsDown.suicide){
					play.setState(Vitality.DEAD);
				} else{
					play.setKeysDown(buttonsDown);
					play.logics();
					play.runEvents();
					
					if(play.tileEvents.size() > 0)
						tileIntersection(play, play.getOccupyingCells());
					
					play.setPrevs();
				}
			} else if(entity instanceof MobileEntity){
				MobileEntity mobile = (MobileEntity) entity;
				
				mobile.logics();
				mobile.runEvents();

				if(mobile.tileEvents.size() > 0)
					tileIntersection(mobile, mobile.getOccupyingCells());
				
				mobile.setPrevs();
			} else {
				entity.runEvents();
			}
		}
	}
	
	private void tileIntersection(MobileEntity mobile, Set<Tile> tiles){
		for(Tile tile : tiles){
			switch(tile){
				case HOLLOW:
					/*/ Do nothing /*/
					break;
				default:
					mobile.runTileEvents(tile);
					break;
			}
		}
	}
	
	private void insertDelete(){
		for(int i = 0; i < awatingObjects.size(); i++){
			Entry<Integer, Entity> entry = awatingObjects.get(i);
			
			if(entry.key-- <= 0){
				if(entry.value instanceof PlayableEntity && ((PlayableEntity)entry.value).isGhost() && (entry.value.id == null || entry.value.id.isEmpty()))
					throw new RuntimeException("Non ghost PlayableEntity must have an id set.");
				
				gameObjects.add(entry.value);
				awatingObjects.remove(i);
				sort = true;
				i--;
				entry.value.level = this;
				entry.value.engine = engine;
				entry.value.present = true;
				entry.value.init();
				
				if(entry.value instanceof PlayableEntity){
					PlayableEntity play = (PlayableEntity) entry.value;
					if(!play.isGhost())
						mainCharacters.add(play);
				}
			}
		}
		
		for(int i = 0; i < deleteObjects.size(); i++){
			Entry<Integer, Entity> entry = deleteObjects.get(i);
			
			if(entry.key-- <= 0){
				gameObjects.remove(entry.value);
				deleteObjects.remove(i);
				i--;
				entry.value.present = false;
				entry.value.dispose();
				
				if(entry.value instanceof PlayableEntity){
					PlayableEntity play = (PlayableEntity) entry.value;
					if(!play.isGhost())
						mainCharacters.remove(play);
				}
			}
		}
	}
}
