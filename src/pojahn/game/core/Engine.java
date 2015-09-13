package pojahn.game.core;

import java.awt.Dimension;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import pojahn.game.essentials.GameState;
import pojahn.game.essentials.HUDMessage;
import pojahn.game.essentials.Image2D;
import pojahn.game.essentials.Keystrokes;
import pojahn.game.essentials.Keystrokes.KeystrokesSession;
import pojahn.game.essentials.Replay;
import pojahn.game.essentials.Vitality;
import pojahn.game.events.Event;
import pojahn.lang.OtherMath;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

public class Engine{

	public float delta = 1.0f / 60.0f;
	public boolean renderText;
	public BitmapFont timeFont;
	public Color timeColor;
	public HUDMessage helpText, winText, deathText, pauseText;
	
	private Level level;
	private SpriteBatch batch;
	private GameState state;
	private Replay replay;
	private List<Replay> replayCache;
	private List<Event> systemEvents;
	private OrthographicCamera gameCamera, hudCamera;
	private Exception exception;
	
	private boolean replaying, flipY, showHelpText, shutdown, flag;
	private int screenWidth, screenHeight, deathCounter;
	private float rotation, musicVolume, prevTx, prevTy, time;
	private long frameCounter;
	
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
		replaying = replay != null;
		replayCache = new Vector<>();
		systemEvents = new ArrayList<>();
		renderText = true;
		timeColor = Color.WHITE;
		screenWidth = screenHeight = -1;
		flipY = true;
	}
	
	public List<Replay> getRecordedReplays(){
		return playingReplay() ? null : replayCache;
	}
	
	public boolean playingReplay(){
		return replaying;
	}
	
	public void addSystemEvent(Event event){
		if(getGameState() != GameState.UNINITIALIZED)
			throw new RuntimeException("Can not add a system event to a game that has been started.");
		
		systemEvents.add(event);
	}
	
	public GameState getGameState(){
		return state;
	}
	
	public double getTimeInSeconds(){
		return OtherMath.round(time, 1);
	}
	
	public int getDeathCounter(){
		return deathCounter;
	}
	
	public long getFrameCounter(){
		return frameCounter;
	}

	public void setScreenSize(int width, int height){
		screenWidth = width;
		screenHeight = height;
		initCameras();
	}
	
	public void setRotation(float rotation){
		gameCamera.rotate(-this.rotation);
		gameCamera.rotate(rotation);
		this.rotation = rotation;
	}
	
	public float getRotation(){
		return rotation;
	}
	
	public void setZoom(float zoom){
		gameCamera.zoom = zoom;
	}
	
	public float getZoom(){
		return gameCamera.zoom;
	}

	public void flipY(){
		flipY = !flipY;
		gameCamera.setToOrtho(flipY);
	}
	
	public void translate(float tx, float ty){
		gameCamera.position.set(tx, ty, 0);
	}
	
	public float tx(){
		return gameCamera.position.x;
	}

	public float ty(){
		return gameCamera.position.y;
	}
	
	public float prevTx(){
		return prevTx;
	}

	public float prevTy(){
		return prevTy;
	}
	
	public Dimension getScreenSize(){
		return new Dimension(screenWidth, screenHeight);
	}
	
	public void gameCamera(){
		batch.setProjectionMatrix(gameCamera.combined);
	}
	
	public void hudCamera(){
		batch.setProjectionMatrix(hudCamera.combined);
	}
	
	public void updateGameCamera(){
		gameCamera.update();
	}
	
	public boolean onScreen(Entity entity){
		Rectangle bbox = Collisions.getBoundingBox(entity.bounds);
		return gameCamera.frustum.boundsInFrustum(bbox.x, bbox.y, 0, bbox.width / 2, bbox.height / 2 , 0);
	}

	public void retry(){
		retry(level.cpPresent());
	}
	
	public void retry(boolean fromCheckpoint){
		if(playingReplay())
			throw new RuntimeException("Can not retry a replay.");
		if(getGameState() == GameState.CRASHED)
			throw new RuntimeException("This instance have crashed an no longer usable.");
		if(getGameState() == GameState.UNINITIALIZED)
			throw new RuntimeException("Can not retry a game that hasn't ben initialized yet.");
		if(getGameState() == GameState.DISPOSED)
			throw new RuntimeException("Can not restart if the resources are disposed.");
		
		restart(fromCheckpoint);
	}
	
	public void exit(){
		GameState s = getGameState();
		if(s == GameState.UNINITIALIZED || s == GameState.DISPOSED)
			throw new IllegalStateException("Can not a exit a game that hasnt been started or been disposed.");
		
		shutdown = true;
	}
	
	public Exception getException(){
		return getGameState() == GameState.CRASHED ? exception : null;
	}
	
	private void setup() throws Exception{
		setGameState(GameState.LOADING);
		batch = new SpriteBatch();
		initCameras();
		level.init();
		level.build();
		
		if(playingReplay())
			level.processMeta(replay.meta);
		
		if(helpText == null)
			helpText = HUDMessage.getCenterizedMessage("Can not pause in replay mode.", getScreenSize(), Color.WHITE);
		if(deathText == null)
			deathText = HUDMessage.getCenterizedMessage("You died, mission failed.", getScreenSize(), Color.WHITE);
		if(winText == null)
			winText = HUDMessage.getCenterizedMessage("Congrats! You completed the level!", getScreenSize(), Color.WHITE);
		if(pauseText == null)
			pauseText = HUDMessage.getCenterizedMessage("Game is paused.", getScreenSize(), Color.WHITE);

		setGameState(GameState.ACTIVE);
	}
	
	private void restart(boolean checkpointPresent){	
		setGameState(GameState.LOADING);
		
		if(getGameState() == GameState.LOST)
			deathCounter++;
		
		level.clean();
		level.build();
		
		if(!checkpointPresent)
			time = 0;
		
		frameCounter = 0;
		flag = false;
		setGameState(GameState.ACTIVE);
	}

	private void overview() {
		if(getGameState() == GameState.DISPOSED)
			throw new IllegalStateException("Can not play a disposed game.");
		if(shutdown)
			throw new RuntimeException("Shutdown");
		
		if(playingReplay() && lost() && level.cpPresent() && !replay.hasEnded())
			restart(true);
			
		for(Event event : systemEvents)
			event.eventHandling();
		
		boolean justResumed = false;
		if(PlayableEntity.checkButtons(level.getAliveMainCharacters()).pause && !playingReplay() && (active() || paused())){
			setGameState(paused() ? GameState.ACTIVE : GameState.PAUSED);
			justResumed = active();
		}

		if(paused()){
			if(!flag){
				flag = true;
				Music music = level.getStageMusic();
				if(music != null){
					musicVolume = music.getVolume();
					music.setVolume(.1f);
				}
			}
			
			renderPause();
		} else {
			if(justResumed){
				setGameState(GameState.ACTIVE);
				Music music = level.getStageMusic();
				if(music != null)
					music.setVolume(musicVolume);

				flag = false;
			}
			
			progress();
			paint();
		}
	}

	private void destroy() {
		setGameState(GameState.DISPOSED);
		batch.dispose();
		level.dispose();
		level = null;
		batch = null;
	}
	
	private void progress(){
		if((lost() || completed()) && !flag){
			flag = true;
			
			if(((lost() && !level.cpPresent()) || completed()) && !playingReplay()){
				finalizeReplay();
				replayCache.add(replay);
				replay = new Replay();
			}
		}
		
		if(frameCounter++ > 2 && active())
			time += Gdx.graphics.getRawDeltaTime();
		
		prevTx = gameCamera.position.x;
		prevTy = gameCamera.position.y;
		
		level.gameLoop();
		statusControll();
	}
	
	private void paint(){
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		updateGameCamera();
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
		if(this.getGameState() == GameState.CRASHED)
			throw new IllegalStateException("This instance have crashed an no longer usable.");
		if(state == GameState.PAUSED && playingReplay())
			throw new IllegalArgumentException("Can not pause when playing a replay.");
		if(this.state == GameState.SUCCESS && (state == GameState.LOST || state == GameState.PAUSED))
			throw new IllegalArgumentException("Can not kill or pause a completed game.");
		
		this.state = state;
	}
	
	private void renderPause(){
		Gdx.gl.glClearColor(0, 0, 0, .4f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		batch.begin();
		
		pauseText.draw(batch, timeFont);

		renderStatusBar();
		
		batch.end();
	}
	
	private void initCameras(){
		screenWidth = screenWidth == -1 ? Gdx.graphics.getWidth() : -1;
		screenHeight = screenHeight == -1 ? Gdx.graphics.getHeight() : -1;
		
		gameCamera = new OrthographicCamera();
		gameCamera.setToOrtho(flipY, screenWidth, screenHeight);
		
		hudCamera = new OrthographicCamera();
		hudCamera.setToOrtho(true, screenWidth, screenHeight);
		
		if(Gdx.graphics.getWidth() != screenWidth || Gdx.graphics.getHeight() != screenHeight)
			Gdx.graphics.setDisplayMode(screenWidth, screenHeight, false);
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
		
		if(dead == total || (alive == 0 && finished == 0))
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
		replay.time = getTimeInSeconds();
		replay.levelClass = level.getClass().getName();
		replay.result = getGameState();
	}
	
	private void renderStatusBar(){
		timeFont.setColor(state == GameState.PAUSED ? Color.WHITE : timeColor);
		timeFont.draw(batch, getTimeInSeconds() + "", 10, 10);
		
		List<PlayableEntity> mains = level.getNonDeadMainCharacters();
		
		for(int index = 0, y = 40; index < mains.size(); index++){
			PlayableEntity main = mains.get(index);
			int hp = main.getHP();
			
			if(main.healthHud != null && main.getState() != Vitality.DEAD && hp > 0){
				Image2D healthHud = main.healthHud.getObject();
				final float width = healthHud.getWidth() + 3;
				
				for(int i = 0, posX = 10; i < hp; i++, posX += width)
					batch.draw(healthHud, posX, y, healthHud.getWidth(), healthHud.getHeight(), 0, 0, healthHud.getWidth(), healthHud.getHeight(), false, true);
				
				y += healthHud.getHeight() + 3;
			}
		}
	}
	
	private void renderStatusText(){
		if(!renderText || timeFont == null)
			return;
		
		if(playingReplay() && PlayableEntity.checkButtons(level.getAliveMainCharacters()).pause)
			showHelpText = !showHelpText;
		
		if(showHelpText && helpText != null)
			helpText.draw(batch, timeFont);

		if(getGameState() == GameState.SUCCESS && winText != null){
			winText.draw(batch, timeFont);
		} else if(getGameState() == GameState.LOST && deathText != null){
			deathText.draw(batch, timeFont);
		}
	}
	
	static class GDXApp extends ApplicationAdapter{

		Engine engine;
		
		@Override
		public void create() {
			try{
				engine.setup();
			}catch(Exception e){
				dispose();
				engine.exception = e;
				engine.setGameState(GameState.CRASHED);
				throw new RuntimeException(e);
			}
		}

		@Override
		public void dispose() {
			engine.destroy();
		}

		@Override
		public void render() {
			try{
				engine.overview();
			}catch(Exception e){
				dispose();
				engine.exception = e;
				engine.setGameState(engine.shutdown ? GameState.DISPOSED : GameState.CRASHED);
				throw new RuntimeException(e);
			}
		}
	}
}