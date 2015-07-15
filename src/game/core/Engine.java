package game.core;

import java.util.List;

import com.badlogic.gdx.ApplicationListener;

import game.essentials.CurrentKeys;

public final class Engine implements ApplicationListener{

	public float delta = 1.0f / 60.0f;
	private Level level;
	private List<CurrentKeys> replayData;
	private boolean replaying;
	
	Engine(Class<Level> level, List<CurrentKeys> replayData) throws InstantiationException, IllegalAccessException{
		this.level = level.newInstance();
		this.level.engine = this;
		this.replayData = replayData;
		this.replaying = replayData != null;
	}
	
	@Override
	public void create() {
		
	}

	@Override
	public void render() {
		
	}

	@Override
	public void dispose() {
		
	}

	@Override public void resize(int x, int u) {}
	@Override public void pause() {}
	@Override public void resume() {}
}
