package game.core;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.time.StopWatch;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import game.essentials.GameState;
import game.essentials.PressedButtons;
import game.essentials.PressedButtons.PressedButtonsSession;
import game.essentials.Replay;
import game.essentials.Vitality;

public class Engine{

	public float delta = 1.0f / 60.0f;
	public BitmapFont timeFont;
	public Color timeColor;
	
	private Level level;
	private SpriteBatch batch;
	private Replay replay;
	private GameState state;
	private StopWatch clock;
	private List<Replay> replayCache;
	private float time;
	private boolean replaying;
	
	public Engine(Level level, Replay replay) throws Exception{
		this.level = level;
		this.level.engine = this;
		this.replay = replay == null ? new Replay() : replay;
		this.replaying = replay != null;
		replayCache = new ArrayList<>();
		state = GameState.UNINITIALIZED;
		batch = new SpriteBatch();
	}
	
	public List<Replay> getRecordedReplays(){
		return replayCache;
	}
	
	public boolean playingReplay(){
		return replaying;
	}
	
	public GameState getGameState(){
		return state;
	}
	
	public void changeLevel(Level newLevel, Object data, boolean resetClock){
		setGameState(GameState.LOADING);
		if(resetClock)
			clock = new StopWatch();
		else
			clock.suspend();
		
		level.clean();
		level.dispose();
		level = newLevel;
		level.init(data);
		level.build(data);
		
		if(!resetClock)
			clock.resume();
		
		setGameState(GameState.PLAYING);
	}
	
	public float getTimeInSeconds(){
		return time / 1000;
	}
	
	private void setup() {
		setGameState(GameState.LOADING);
		level.init(null);
		restart();
		setGameState(GameState.PLAYING);
	}

	private void overview() {
		try{
			if(Gdx.input.isKeyJustPressed(Keys.ESCAPE)){
				if(!clock.isSuspended())
					clock.suspend();
				
				renderPause();
				setGameState(state == GameState.PAUSED ? GameState.PLAYING : GameState.PAUSED);
			} else{
				if(clock.isSuspended())
					clock.resume();
				
				progress();
				paint();
			}
		} catch(Exception e){
			destroy();	//TODO: Denna kanske redan kallas av LibGDX containern. Testa!
			throw e;
		}
	}

	private void destroy() {
		state = GameState.DISPOSED;
		batch.dispose();
		level.dispose();
		level = null;
	}
	
	private void progress(){
		time = clock.getTime();
		level.gameLoop();
	}
	
	private void paint(){
		batch.begin();
		
		for(Entity entity : level.gameObjects)
			entity.render(batch);
		
		renderStatusBar();
		
		batch.end();
	}
	
	private void restart(){
		setGameState(GameState.LOADING);
		finalizeReplay();
		replayCache.add(replay);
		replay = new Replay();
		time = 0;
		level.clean();
		level.build(null);
		clock = new StopWatch();
		clock.start();
	}

	private void setGameState(GameState state){
		if(state == GameState.PAUSED && replaying)
			return;
		if(this.state == GameState.FINISHED && (state == GameState.DEAD || state == GameState.PAUSED))
			return;
		
		this.state = state;
	}
	
	private void renderPause(){
		batch.begin();
		Gdx.gl.glClearColor(0, 0, 0, .4f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		timeFont.setColor(Color.WHITE);
		timeFont.draw(batch, "Game is paused.", Gdx.graphics.getWidth() / 2 - 120, Gdx.graphics.getHeight() / 2);

		renderStatusBar();
		
		batch.end();
	}
	
	void registerReplayFrame(PlayableEntity entity, PressedButtons buttonsDown){
		PressedButtonsSession currButtonSession = null;
		
		for(PressedButtonsSession buttonSession : replay.data){
			if(buttonSession.id.equals(entity.id)){
				currButtonSession = buttonSession;
				break;
			}
		}
		
		if(currButtonSession == null){
			currButtonSession = new PressedButtonsSession(entity.id);
			replay.data.add(currButtonSession);
		}
		
		currButtonSession.sessionKeys.add(buttonsDown);
	}
	
	PressedButtons getReplayFrame(PlayableEntity play){
		for(PressedButtonsSession buttonSession : replay.data)
			if(buttonSession.id.equals(play.id))
				return buttonSession.next();
		
		throw new RuntimeException("No replay found for the entity: " + play.id);
	}
	
	private void finalizeReplay(){
		replay.date = ZonedDateTime.now();;
		replay.time = time;
		replay.levelClass = level.getClass().getName();
	}
	
	private void renderStatusBar(){
		timeFont.setColor(state == GameState.PAUSED ? Color.WHITE : timeColor);
		timeFont.draw(batch, time / 1000 + "", 10, 10);

		List<PlayableEntity> mains = level.getNonDeadMainCharacters();
		
		for(int index = 0, y = 40; index < mains.size(); index++)
		{
			PlayableEntity main = mains.get(index);
			int hp = main.getHP();
			
			if(main.healthHud != null && main.getState() != Vitality.DEAD && hp > 0)
			{
				final float width = main.healthHud.getWidth() + 3;
				
				for(int i = 0, posX = 10; i < hp; i++, posX += width)
					batch.draw(main.healthHud, posX, y);
				
				y += main.healthHud.getHeight() + 3;
			}
		}
	}
	
	static class GDXApp extends ApplicationAdapter{

		Engine engine;
		
		@Override
		public void create() {
			engine.setup();
		}

		@Override
		public void dispose() {
			engine.destroy();
		}

		@Override
		public void render() {
			engine.overview();
		}
	}
}