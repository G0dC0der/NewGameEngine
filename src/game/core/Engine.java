package game.core;

import game.essentials.CurrentKeys.MultiCurrentKeys;
import game.essentials.GameState;
import game.essentials.Replay;
import game.events.Event;

import java.util.List;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public final class Engine implements ApplicationListener{

	public float delta = 1.0f / 60.0f;
	
	GameState gameState;
	private Level level;
	private List<MultiCurrentKeys> replayData;
	private SpriteBatch batch;
	private List<Replay> recordedReplays;
	private Event exitEvent;
	private boolean replaying;
	
	Engine(Class<? extends Level> level, List<MultiCurrentKeys> replayData) throws Exception{
		this.level = level.newInstance();
		this.level.engine = this;
		this.replayData = replayData;
		this.replaying = replayData != null;
		batch = new SpriteBatch();
	}
	
	@Override
	public void create() {
		
	}

	@Override
	public void render() {
		progress();
		paint();
	}

	@Override
	public void dispose() {
		if(exitEvent != null)
			exitEvent.eventHandling();
	}
	
	public void setExitEvent(Event exitEvent){
		this.exitEvent = exitEvent;
	}
	
	public List<Replay> getReplays(){
		return recordedReplays;
	}
	
	private void progress(){
		
	}
	
	private void paint(){
		batch.begin();
		
		for(Entity entity : level.gameObjects)
			entity.render(batch);
		
		batch.end();
	}

	@Override public void resize(int x, int u) {}
	@Override public void pause() {}
	@Override public void resume() {}
}
