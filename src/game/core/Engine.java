package game.core;

import java.awt.Dimension;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.time.StopWatch;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import game.essentials.GameState;
import game.essentials.Image2D;
import game.essentials.Keystrokes;
import game.essentials.Keystrokes.KeystrokesSession;
import game.essentials.Replay;
import game.essentials.Vitality;

public class Engine{

	public float delta = 1.0f / 60.0f;
	public boolean renderText;
	public BitmapFont timeFont;
	public Color timeColor;
	public String helpText = "You are in replay mode thus can not pause.";
	public String failText = "You are dead.";
	public String winText  = "Congratulations! It took you %d seconds to complete the map!";
	public String playerName = "Player";
	
	private Level level;
	private SpriteBatch batch;
	private GameState state;
	private StopWatch clock;
	private Replay replay;
	private List<Replay> replayCache;
	private Map<String, Object> vars;
	private OrthographicCamera camera, gameCamera, hudCamera;
	
	public static Engine playEngine(Level level){
		return new Engine(level,null);
	}
	
	public static Engine replayEngine(Level level, Replay replay){
		return new Engine(level,replay);
	}
	
	private Engine(Level level, Replay replay){
		if(level == null)
			throw new NullPointerException("The level can not be null.");
		
		state = GameState.UNINITIALIZED;
		this.level = level;
		this.level.engine = this;
		this.replay = replay == null ? new Replay() : replay;
		replayCache = new ArrayList<>();
		renderText = true;
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
		return (float)clock.getTime() / 1000.0f;
	}
	
	public long getTime(){
		return clock.getTime();
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
	
	public float getZoom(){
		return (float) vars.get("zoom");
	}
	
	public void translate(float tx, float ty){
		vars.put("tx", tx);
		vars.put("ty", ty);
	}
	
	public void flipY(boolean flip){
		vars.put("flipY", flip);
	}
	
	public Vector2 getTranslation(){
		return new Vector2((float)vars.get("tx"), (float)vars.get("ty"));
	}
	
	public Vector2 getPreviousTranslation(){
		return (Vector2) vars.get("prevTranslate");
	}
	
	public Dimension getScreenSize(){
		return new Dimension((int) Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
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
	
	public OrthographicCamera getCamera(){
		return camera;
	}
	
	public void retry(){
		retry(level.safeRestart());
	}
	
	public boolean onScreen(Entity entity){
		Rectangle bbox = Collisions.getBoundingBox(entity);
		return camera.frustum.boundsInFrustum(bbox.x, bbox.y, 0, bbox.width / 2, bbox.height / 2 , 0);
	}
	
	public void retry(boolean fromCheckpoint){
		if(getGameState() == GameState.UNINITIALIZED)
			throw new RuntimeException("Can not retry a game that hasn't ben initialized yet.");
		if(fromCheckpoint && getGameState() == GameState.SUCCESS)
			throw new RuntimeException("Can not restart a finished game from checkpoint.");
		if(getGameState() == GameState.DISPOSED)
			throw new RuntimeException("Can not restart if the resources are disposed.");
		
		restart(fromCheckpoint);
	}
	
	public void exit(){
		Gdx.app.exit(); //Test: Should not exit other non-daemon threads. Should also call destroy()/dispose()
	}
	
	private void setup() {
		setGameState(GameState.LOADING);
		batch = new SpriteBatch();
		initCameras();
		level.init();
		level.build();
		clock = new StopWatch();
		
		if(playingReplay())
			level.processMeta(replay.meta);

		clock.start();
		setGameState(GameState.ACTIVE);
	}
	
	private void restart(boolean checkpointPresent){
		if(!clock.isSuspended())
			clock.suspend();
		
		if(getGameState() == GameState.LOST)
			vars.put("deaths", ((int)vars.get("deaths")) + 1);
		
		setGameState(GameState.LOADING);
		
		if(!checkpointPresent && !playingReplay()){
			finalizeReplay();
			replayCache.add(replay);
			replay = new Replay();
		}
		
		level.clean();
		level.build();
		clock.resume();
		
		if(!checkpointPresent)
			clock.reset();
		
		setGameState(GameState.ACTIVE);
	}

	private void overview() {
		if(getGameState() == GameState.DISPOSED)
			throw new IllegalStateException("Can not play a disposed game.");
		
		if((getGameState() == GameState.ACTIVE || getGameState() == GameState.PAUSED) && !playingReplay() && PlayableEntity.keysDown(level.getAliveMainCharacters()).pause){
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
				setGameState(GameState.ACTIVE);
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
		batch = null;
	}
	
	private void progress(){
		statusControll();
		
		if((lost() || completed()) && !clock.isSuspended()){
			clock.suspend();
		}
		
		vars.put("prevTranslate", getTranslation());
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
	
	private void setGameState(GameState state){
		if(state == GameState.PAUSED && playingReplay())
			throw new IllegalArgumentException("Can not pause when playing a replay.");
		if(this.state == GameState.SUCCESS && (state == GameState.LOST || state == GameState.PAUSED))
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
		
		if(dead == total || alive == 0)
			setGameState(GameState.LOST);
		else if(finished > 0)
			setGameState(GameState.SUCCESS);
		else if(alive > 0)
			setGameState(GameState.ACTIVE);
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
	
	boolean lost(){
		return getGameState() == GameState.LOST;
	}
	
	boolean completed(){
		return getGameState() == GameState.SUCCESS;
	}
	
	boolean paused(){
		return getGameState() == GameState.PAUSED;
	}
	
	boolean active(){
		return getGameState() == GameState.ACTIVE;
	}
	
	private void finalizeReplay(){
		replay.date = ZonedDateTime.now();;
		replay.time = clock.getTime();
		replay.levelClass = level.getClass().getName();
		replay.playerName = playerName;
	}
	
	private void renderStatusBar(){
		timeFont.setColor(state == GameState.PAUSED ? Color.WHITE : timeColor);
		timeFont.draw(batch, getTime() + "", 10, 10);

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
		if(!renderText)
			return;
		
		if(playingReplay() && PlayableEntity.keysDown(level.getAliveMainCharacters()).pause)
			vars.put("helpText", !((boolean)vars.get("helpText")));
		
		if((boolean) vars.get("helpText")){
			timeFont.draw(batch, helpText, Gdx.graphics.getWidth() / 3, Gdx.graphics.getHeight() / 2);
		}

		if(!playingReplay() && getGameState() == GameState.SUCCESS){
			timeFont.draw(batch, String.format(winText, getTime()), Gdx.graphics.getWidth() / 4, Gdx.graphics.getHeight() / 2);
		} else if(!playingReplay() && getGameState() == GameState.LOST){
			timeFont.draw(batch, failText, Gdx.graphics.getWidth() / 3, Gdx.graphics.getHeight() / 2);
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