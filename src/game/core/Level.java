package game.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.badlogic.gdx.audio.Music;

import game.essentials.GameState;
import game.essentials.Keystrokes;
import game.essentials.Utils;
import game.essentials.Vitality;
import game.events.Event;
import game.events.TaskEvent;
import game.lang.Entry;

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
		mainCharacters = new ArrayList<>();
		gameObjects = new LinkedList<>();
	}
	
	public abstract int getWidth();

	public abstract int getHeight();
	
	public abstract Tile tileAt(int x, int y);
	
	public abstract boolean isSolid(int x, int y);

	public abstract boolean isHollow(int x, int y);
	
	public abstract void init();
	
	public abstract void build();
	
	public abstract void dispose();
	
	public void processMeta(Serializable meta) {}

	public Serializable getMeta(){
		return null;
	}
	
	public boolean safeRestart(){
		return false;
	}
	
	public Music getStageMusic(){
		return null;
	}
	
	public Engine getEngine(){
		return engine;
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
	
	public Entity addTemp(Event event, int lifeFrames){
		Entity wrapper = Utils.wrap(event);
		add(wrapper);
		discardAfter(wrapper, lifeFrames);
		
		return wrapper;
	}
	
	public Entity addTemp(Event event, TaskEvent discardEvent){
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
	
	
	void gameLoop(){
		insertDelete();
		
		if(sort){
			Collections.sort(gameObjects, Z_INDEX_SORT);
			sort = false;
		}
		
		update();
	}
	
	void clean(){
		awatingObjects.clear();
		deleteObjects.clear();
		gameObjects.forEach(e -> e.dispose());
		gameObjects.clear();
	}
	
	private void update(){
		mainCharacters.clear();
		
		for(Entity entity : gameObjects){
			if(!entity.isActive())
				continue;
			
			if(entity instanceof PlayableEntity){
				PlayableEntity play = (PlayableEntity) entity;
				Keystrokes buttonsDown;
				
				if(play.isGhost())
					buttonsDown = play.nextReplayFrame();
				else if(engine.getGameState() == GameState.ACTIVE){
					if(play.getState() == Vitality.ALIVE)
						buttonsDown = engine.playingReplay() ? engine.getReplayFrame(play) : PlayableEntity.checkButtons(play.getController());
					else
						buttonsDown = PlayableEntity.STILL;
				} else
					buttonsDown = PlayableEntity.STILL;
				
				if(!play.isGhost())
					mainCharacters.add(play);
				
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
					
					play.prevX = play.x();
					play.prevY = play.y();
				}
				
				if(play.getState() == Vitality.DEAD)
					play.deathAction();
			} else if(entity instanceof MobileEntity){
				MobileEntity mobile = (MobileEntity) entity;
				
				mobile.logics();
				mobile.runEvents();

				if(mobile.tileEvents.size() > 0)
					tileIntersection(mobile, mobile.getOccupyingCells());
				
				mobile.prevX = mobile.x();
				mobile.prevY = mobile.y();
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
				entry.value.move(-9999, -9999);
			}
		}
	}
}
