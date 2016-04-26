package pojahn.game.core;

import java.awt.Dimension;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import pojahn.game.essentials.ControlledException;
import pojahn.game.essentials.GameState;
import pojahn.game.essentials.HUDMessage;
import pojahn.game.essentials.Image2D;
import pojahn.game.essentials.Keystrokes;
import pojahn.game.essentials.Keystrokes.KeystrokeSession;
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

/**
 * An instance of Engine is used to play one course.
 * To play a new course, dispose this one and create a new instance.
 */
public class Engine{

    /*
     * TODO: Check how dispose is called. When? How many times during crash and controlled exit?
     * TODO: Check of often setGameChange is executed when a state is changed. It should be once.
     */

	public float delta = 1.0f / 60.0f;
	public boolean renderText;
	public BitmapFont timeFont;
	public Color timeColor;
	public HUDMessage helpText, winText, deathText, pauseText;
	
	private final Level level;
	private SpriteBatch batch;
	private GameState state;
	private Replay replay;
	private List<Replay> recordings;
	private OrthographicCamera gameCamera, hudCamera;
    private Map<GameState, Event> stateEvents;
	private Exception exception;
	
	private boolean replaying, flipY, showHelpText, saveReplayAllowed, shutdown;
	private int screenWidth, screenHeight, deathCounter;
	private float rotation, musicVolume, prevTx, prevTy, time;
	private long frameCounter;
	
	public Engine(Level level, Replay replay){
		if(level == null)
			throw new NullPointerException("The level can not be null.");
		
		state = GameState.UNINITIALIZED;
		this.level = level;
		this.level.engine = this;
		this.replay = replay == null ? new Replay() : replay;
        this.saveReplayAllowed = replay == null;
		replaying = replay != null;
		recordings = new Vector<>();
        stateEvents = new HashMap<>();
		renderText = true;
		timeColor = Color.WHITE;
		flipY = true;
	}
	
	public List<Replay> getRecordings(){
		return isReplaying() ? null : new ArrayList<>(recordings);
	}
	
	public boolean isReplaying(){
		return replaying;
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
		if(isReplaying())
			throw new RuntimeException("Can not retry a replay.");
		if(getGameState() == GameState.CRASHED)
			throw new RuntimeException("This instance have crashed an no longer usable.");
		if(getGameState() == GameState.UNINITIALIZED)
			throw new RuntimeException("Can not retry a game that hasn't ben initialized yet.");
		if(getGameState() == GameState.DISPOSED)
			throw new RuntimeException("Can not restart if the resources are disposed.");
		
		restart(fromCheckpoint);
	}
	
	public void exitGame(){
		shutdown = true;
	}
	
	public Exception getException(){
		return getGameState() == GameState.CRASHED ? exception : null;
	}

    public void setGameStateEvent(GameState gameState, Event event) {
        stateEvents.put(gameState, event);
    }
	
	private void setup() throws Exception{
		setGameState(GameState.LOADING);
		batch = new SpriteBatch();
		initCameras();
		level.init();
		level.build();
		
		if(isReplaying())
			level.processMeta(replay.meta);

        Dimension screenSize = getScreenSize();
		helpText = HUDMessage.getCenteredMessage("Can not pause in replay mode.", screenSize, Color.WHITE);
		deathText = HUDMessage.getCenteredMessage("You died, mission failed.", screenSize, Color.WHITE);
		winText = HUDMessage.getCenteredMessage("Congrats! You completed the level!", screenSize, Color.WHITE);
		pauseText = HUDMessage.getCenteredMessage("Game is paused.", screenSize, Color.WHITE);

		setGameState(GameState.ACTIVE);
	}
	
	private void restart(boolean checkpointPresent){
		if(getGameState() == GameState.LOST)
			deathCounter++;

		setGameState(GameState.LOADING);

		level.clean();
		level.build();
		
		if(!checkpointPresent)
			time = 0;
		
		frameCounter = 0;
        saveReplayAllowed = !isReplaying();
		setGameState(GameState.ACTIVE);
	}

	private void overview() {
        if(shutdown)
            throw new ControlledException("The game was terminated in a controlled way.");

        if(!isReplaying()) {
            if(PlayableEntity.mergeButtons(level.getAliveMainCharacters()).pause && (active() || paused())){
                setGameState(paused() ? GameState.ACTIVE : GameState.PAUSED);

                if(active()) {
                    Music music = level.getStageMusic();
                    if(music != null)
                        music.setVolume(musicVolume);

                } else if(paused()) {
                    Music music = level.getStageMusic();
                    if(music != null){
                        musicVolume = music.getVolume();
                        music.setVolume(.1f);
                    }
                }
            }

            if(saveReplayAllowed && (lost() || completed())){
                saveReplayAllowed = false;
                finalizeReplay();
            }
        } else {
            if(lost() && level.cpPresent() && !replay.hasEnded()) {
                restart(true);
            }
        }

		if(paused()){
			renderPause();
		} else {
			progress();
			paint();
		}
	}

	private void destroy() {
		batch.dispose();
		level.dispose();
		batch = null;
	}
	
	private void progress(){
		if(frameCounter++ > 2 && active()) //TODO: Why the fuck must frameCounter exceed 2? o_0
			time += Gdx.graphics.getRawDeltaTime();
		
		prevTx = gameCamera.position.x;
		prevTy = gameCamera.position.y;
		
		level.gameLoop();
		statusControl();
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
		if(state == GameState.PAUSED && isReplaying())
			throw new IllegalArgumentException("Can not pause when playing a replay.");
		if(this.state == GameState.SUCCESS && (state == GameState.LOST || state == GameState.PAUSED))
			throw new IllegalArgumentException("Can not kill or pause a completed game.");

        if(state == this.getGameState())
            return;
		
		this.state = state;

        Event event = stateEvents.get(this.state);
        if(event != null) {
            new Thread(event::eventHandling).start();
        }
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
		screenWidth = screenWidth == 0 ? Gdx.graphics.getWidth() : screenWidth;
		screenHeight = screenHeight == 0 ? Gdx.graphics.getHeight() : screenHeight;
		
		gameCamera = new OrthographicCamera();
		gameCamera.setToOrtho(flipY, screenWidth, screenHeight);
		
		hudCamera = new OrthographicCamera();
		hudCamera.setToOrtho(true, screenWidth, screenHeight);

		if(Gdx.graphics.getWidth() != screenWidth || Gdx.graphics.getHeight() != screenHeight)
			Gdx.graphics.setWindowedMode(screenWidth, screenHeight);
	}
	
	private void statusControl(){
		List<PlayableEntity> mains = level.getMainCharacters();
		int total = mains.size();
		long alive = mains.stream().filter(PlayableEntity::isAlive).count();
		long dead = mains.stream().filter(PlayableEntity::isDead).count();
		long finished = mains.stream().filter(PlayableEntity::isDone).count();;

		if(dead == total || (alive == 0 && finished == 0))
			setGameState(GameState.LOST);
		else if(finished > 0)
			setGameState(GameState.SUCCESS);
		else if(alive > 0)
			setGameState(GameState.ACTIVE);
		else
			throw new IllegalStateException("Game is in a unknown state.");
	}
	
	void registerReplayFrame(PlayableEntity play, Keystrokes keystrokes){
		KeystrokeSession currButtonSession = null;
		
		for(KeystrokeSession buttonSession : replay.data){
			if(buttonSession.identifier.equals(play.identifier)){
				currButtonSession = buttonSession;
				break;
			}
		}
		
		if(currButtonSession == null){
			currButtonSession = new KeystrokeSession(play);
			replay.data.add(currButtonSession);
		}
		
		currButtonSession.sessionKeys.add(keystrokes);
	}
	
	Keystrokes getReplayFrame(PlayableEntity play){
		for(KeystrokeSession buttonSession : replay.data)
			if(buttonSession.identifier.equals(play.identifier))
				return buttonSession.next();
		
		throw new RuntimeException("No replay found for the character.");
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
		replay.date = ZonedDateTime.now();
		replay.time = getTimeInSeconds();
		replay.levelClass = level.getLevelName();
		replay.result = getGameState();
        recordings.add(replay);
        replay = new Replay();
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
		
		if(isReplaying() && PlayableEntity.mergeButtons(level.getAliveMainCharacters()).pause)
			showHelpText = !showHelpText;
		
		if(showHelpText && helpText != null)
			helpText.draw(batch, timeFont);

		if(getGameState() == GameState.SUCCESS && winText != null){
			winText.draw(batch, timeFont);
		} else if(getGameState() == GameState.LOST && deathText != null){
			deathText.draw(batch, timeFont);
		}
	}

    static ApplicationAdapter wrap(Engine engine) {
        return new ApplicationAdapter() {
            @Override
            public void dispose() {
                engine.destroy();
            }

            @Override
            public void render() {
                try{
                    engine.overview();
                } catch (ControlledException e) {
                    engine.setGameState(GameState.DISPOSED);
                    throw e;
                } catch (Exception e){
                    engine.exception = e;
                    engine.setGameState(GameState.CRASHED);
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void create() {
                try {
                    engine.setup();
                } catch (Exception e) {
                    engine.exception = e;
                    engine.setGameState(GameState.CRASHED);
                    throw new RuntimeException(e);
                }
            }
        };
    }
}