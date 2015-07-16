package game.core;

import java.util.List;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import game.essentials.CurrentKeys.MultiCurrentKeys;

public final class Engine implements ApplicationListener{

	public float delta = 1.0f / 60.0f;
	private Level level;
	private List<MultiCurrentKeys> replayData;
	private SpriteBatch batch;
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
