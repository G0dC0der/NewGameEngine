package game.core;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.time.StopWatch;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import game.essentials.GameState;
import game.essentials.Image2D;
import game.essentials.Keystrokes;
import game.essentials.Keystrokes.KeystrokesSession;
import game.essentials.Replay;
import game.essentials.Vitality;

public class Engine{

	public float delta = 1.0f / 60.0f;
	public BitmapFont timeFont;
	public Color timeColor;
	
	private Level level;
	private SpriteBatch batch;
	private GameState state;
	private StopWatch clock;
	private Replay replay;
	private List<Replay> replayCache;
	private Map<String, Object> vars;
	private OrthographicCamera camera, gameCamera, hudCamera;
	private long time;
	
	public Engine(Level level, Replay replay){
		state = GameState.UNINITIALIZED;

		if(level == null)
			throw new NullPointerException("The level can not be null.");
		
		this.level = level;
		this.level.engine = this;
		this.replay = replay == null ? new Replay() : replay;
		replayCache = new ArrayList<>();
		vars = new HashMap<>();
		vars.put("replaying", replay != null);
		vars.put("screenWidth", 800);
		vars.put("screenHeight", 600);
		vars.put("sceenScale", 1);
		vars.put("helpText", false);
		vars.put("deaths", -1);
	}
	
	public List<Replay> getRecordedReplays(){
		return replayCache;
	}
	
	public boolean playingReplay(){
		return (boolean) vars.get("replaying");
	}
	
	public GameState getGameState(){
		return state;
	}
	
	public float getTimeInSeconds(){
		return (float)time / 1000.0f;
	}
	
	public long getTime(){
		return time;
	}
	
	public int getDeathCounter(){
		return (int) vars.get("deaths");
	}
	
	public Exception getException(){
		return (Exception) vars.get("exception");
	}
	
	public void setScreenSize(int width, int height){
		vars.put("screenWidth", width);
		vars.put("screenHeight", height);
		initCameras();
	}
	
	public void setScreenScale(float multiplier){
		vars.put("screenScale", multiplier);
		initCameras();
	}
	
	public void setRotation(float rotation){
		vars.put("rotation", rotation);
	}
	
	public void setZoom(float zoom){
		vars.put("zoom", zoom);
	}
	
	public void translate(float tx, float ty){
		vars.put("tx", tx);
		vars.put("ty", ty);
	}
	
	public void flipY(boolean flip){
		vars.put("flipY", flip);
	}
	
	public void gameCamera(){
		camera = gameCamera;
		batch.setProjectionMatrix(camera.combined);
	}
	
	public void hudCamera(){
		camera = hudCamera;
		batch.setProjectionMatrix(camera.combined);
	}
	
	public void updateCamera(){
		adjustLens(camera);
		camera.update();
	}
	
//	public boolean retry(){
//		
//	}
	
	public void exit(){
		Gdx.app.exit(); //Test: Should not exit other non-daemon threads. Should also call destroy()/dispose()
	}
	
	private void setup() {
		setGameState(GameState.LOADING);
		batch = new SpriteBatch();
		initCameras();
		level.init();
		restart();
		if(playingReplay())
			level.processMeta(replay.meta);
		setGameState(GameState.PLAYING);
	}

	private void overview() {
		if(getGameState() == GameState.DISPOSED)
			throw new IllegalStateException("Can not play a disposed game.");
		
		if(!playingReplay() && Gdx.input.isKeyJustPressed(Keys.R) && (getGameState() == GameState.DEAD || getGameState() == GameState.FINISHED)){
			if(Gdx.input.isKeyJustPressed(Keys.R))
				restart();
			else if(Gdx.input.isKeyJustPressed(Keys.Q)){
				Gdx.app.exit(); //Test: Should not exit other non-daemon threads. Should also call destroy()/dispose()
			}
		}
		
		if(PlayableEntity.keysDown(level.getAliveMainCharacters()).pause && !playingReplay() && getGameState() != GameState.DEAD && getGameState() != GameState.FINISHED){
			if(!clock.isSuspended()){
				clock.suspend();
				setGameState(GameState.PAUSED);
				Music music = level.getStageMusic();
				if(music != null){
					vars.put("musicVolume", music.getVolume());
					music.setVolume(.1f);
				}
			}
			
			renderPause();
		} else{
			if(clock.isSuspended()){
				setGameState(GameState.PLAYING);
				Music music = level.getStageMusic();
				if(music != null)
					music.setVolume((float)vars.get("musicVolume"));

				clock.resume();
			}
			
			progress();
			paint();
		}
	}

	private void destroy() {
		state = GameState.DISPOSED;
		batch.dispose();
		level.dispose();
		level = null;
	}
	
	private void progress(){
		statusControll();
		
		if(getGameState() == GameState.DEAD || getGameState() == GameState.FINISHED && !clock.isStopped()){
			clock.stop();
		}
		
		time = clock.getTime();
		level.gameLoop();
	}
	
	private void paint(){
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		updateCamera();
		gameCamera();
		
		batch.begin();
		
		for(Entity entity : level.gameObjects)
			entity.render(batch);
		
		hudCamera();
		renderStatusBar();
		renderStatusText();
		
		batch.end();
	}
	
	private void restart(){
		if(getGameState() == GameState.DEAD)
			vars.put("deaths", ((int)vars.get("deaths")) + 1);
		
		setGameState(GameState.LOADING);
		finalizeReplay();
		if(!playingReplay()){
			replayCache.add(replay);
			replay = new Replay();
		}
		time = 0;
		level.clean();
		level.build();
		clock = new StopWatch();
		clock.start();
	}

	private void setGameState(GameState state){
		if(state == GameState.PAUSED && playingReplay())
			throw new IllegalArgumentException("Can not pause when playing a replay.");
		if(this.state == GameState.FINISHED && (state == GameState.DEAD || state == GameState.PAUSED))
			throw new IllegalArgumentException("Can not kill or pause a completed game.");
		
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
	
	private void initCameras(){
		int windowWidth 	= (int) 	vars.get("screenWidth");
		int windowHeight 	= (int) 	vars.get("screenHeight");
		float scale 		= (float) 	vars.get("screenScale");
		boolean flipY 		= (boolean) vars.get("flipY");
		
		gameCamera = new OrthographicCamera();
		gameCamera.setToOrtho(flipY, windowWidth, windowHeight);
		
		hudCamera = new OrthographicCamera();
		hudCamera.setToOrtho(true, windowWidth, windowHeight);
		
		if(Gdx.graphics.getWidth() != windowWidth * scale || Gdx.graphics.getHeight() != windowHeight * scale)
			Gdx.graphics.setDisplayMode((int)(windowWidth * scale), (int)(windowHeight * scale), false);
	}
	
	private void adjustLens(OrthographicCamera camera){
		float tx 		= (float) vars.get("tx");
		float ty 		= (float) vars.get("ty");
		float zoom 		= (float) vars.get("zoom");
		float rotation 	= (float) vars.get("rotation");
		
		camera.position.set(tx,ty,0);
		camera.rotate(rotation);
		camera.zoom = zoom;
	}
	
	private void statusControll(){
		List<PlayableEntity> mains = level.getMainCharacters();
		int total = mains.size();
		int alive = 0;
		int dead = 0;
		int finished = 0;
		
		for(PlayableEntity play  : mains){
			switch(play.getState()){
			case ALIVE:
				alive++;
				break;
			case DEAD:
				dead++;
				break;
			case COMPLETED:
				finished++;
				break;
			}
		}
		
		if(dead == total)
			setGameState(GameState.DEAD);
		else if(finished > 0)
			setGameState(GameState.FINISHED);
		else if(alive > 0)
			setGameState(GameState.PLAYING);
		else
			throw new IllegalStateException("Game is in a unknown state.");
	}
	
	void registerReplayFrame(PlayableEntity entity, Keystrokes buttonsDown){
		KeystrokesSession currButtonSession = null;
		
		for(KeystrokesSession buttonSession : replay.data){
			if(buttonSession.id.equals(entity.id)){
				currButtonSession = buttonSession;
				break;
			}
		}
		
		if(currButtonSession == null){
			currButtonSession = new KeystrokesSession(entity.id);
			replay.data.add(currButtonSession);
		}
		
		currButtonSession.sessionKeys.add(buttonsDown);
	}
	
	Keystrokes getReplayFrame(PlayableEntity play){
		for(KeystrokesSession buttonSession : replay.data)
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
				Image2D healthHud = main.healthHud.getObject();
				final float width = healthHud.getWidth() + 3;
				
				for(int i = 0, posX = 10; i < hp; i++, posX += width)
					batch.draw(healthHud, posX, y);
				
				y += healthHud.getHeight() + 3;
			}
		}
	}
	
	private void renderStatusText(){
		if(playingReplay() && PlayableEntity.keysDown(level.getAliveMainCharacters()).pause)
			vars.put("helpText", !((boolean)vars.get("helpText")));
		
		boolean helpText = (boolean) vars.get("helpText");
		//Help text
		
		//Win text
		
		//Dead text
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