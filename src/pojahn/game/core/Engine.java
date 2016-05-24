package pojahn.game.core;

import java.awt.Dimension;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.stream.Collectors;

import com.badlogic.gdx.ApplicationListener;
import pojahn.game.essentials.*;
import pojahn.game.essentials.recording.KeySession;
import pojahn.game.essentials.recording.PlaybackRecord;
import pojahn.game.essentials.recording.Replay;
import pojahn.game.events.Event;
import pojahn.lang.OtherMath;

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
public final class Engine {

    public float delta = 1.0f / 60.0f;
    public boolean renderText;
    public BitmapFont timeFont;
    public Color timeColor;
    public HUDMessage helpText, winText, deathText, deathCheckpointText, pauseText;

    private final Level level;
    private SpriteBatch batch;
    private GameState state;
    private PlaybackRecord playback;
    private List<Replay> recordings;
    private OrthographicCamera gameCamera, hudCamera;
    private Map<GameState, Event> stateEvents;
    private FutureObject<Exception> exception;

    private boolean replaying, flipY, showHelpText, shutdown;
    private int screenWidth, screenHeight, deathCounter;
    private float rotation, musicVolume, prevTx, prevTy, time;
    private long frameCounter, uniqueCounter;

    public Engine(Level level, PlaybackRecord replayData) {
        if (level == null)
            throw new NullPointerException("The level can not be null.");

        state = GameState.UNINITIALIZED;
        this.level = level;
        this.level.engine = this;
        this.playback = replayData;
        replaying = replayData != null;
        recordings = new Vector<>();
        stateEvents = new HashMap<>();
        exception = new FutureObject<>();
        renderText = true;
        timeColor = Color.WHITE;
        flipY = true;
    }

    public List<Replay> getRecordings() {
        if (!isReplaying()) {
            List<Replay> recordings = new ArrayList<>(this.recordings);
            this.recordings.clear();
            return recordings;
        }
        return null;
    }

    public boolean isReplaying() {
        return replaying;
    }

    public GameState getGameState() {
        return state;
    }

    public double getTimeInSeconds() {
        return OtherMath.round(time, 1);
    }

    public int getDeathCounter() {
        return deathCounter;
    }

    public long getFrameCounter() {
        return frameCounter;
    }

    public void setScreenSize(int width, int height) {
        screenWidth = width;
        screenHeight = height;
        initCameras();
    }

    public void setRotation(float rotation) {
        gameCamera.rotate(-this.rotation);
        gameCamera.rotate(rotation);
        this.rotation = rotation;
    }

    public OrthographicCamera getGameCamera() {
        return gameCamera;
    }

    public float getRotation() {
        return rotation;
    }

    public void setZoom(float zoom) {
        gameCamera.zoom = zoom;
    }

    public float getZoom() {
        return gameCamera.zoom;
    }

    public void flipY() {
        flipY = !flipY;
        gameCamera.setToOrtho(flipY);
    }

    public boolean flippedY() {
        return !flipY;
    }

    public void translate(float tx, float ty) {
        gameCamera.position.set(tx, ty, 0);
    }

    public float tx() {
        return gameCamera.position.x;
    }

    public float ty() {
        return gameCamera.position.y;
    }

    public float prevTx() {
        return prevTx;
    }

    public float prevTy() {
        return prevTy;
    }

    public Dimension getScreenSize() {
        return new Dimension(screenWidth, screenHeight);
    }

    public void gameCamera() {
        batch.setProjectionMatrix(gameCamera.combined);
    }

    public void hudCamera() {
        batch.setProjectionMatrix(hudCamera.combined);
    }

    public void updateGameCamera() {
        gameCamera.update();
    }

    public boolean onScreen(Entity entity) {
        Rectangle bbox = Collisions.getBoundingBox(entity.bounds);
        return gameCamera.frustum.boundsInFrustum(bbox.x, bbox.y, 0, bbox.width / 2, bbox.height / 2, 0);
    }

    public void retry() {
        retry(level.cpPresent());
    }

    public void retry(boolean fromCheckpoint) {
        if (isReplaying())
            throw new RuntimeException("Can not retry a replay.");
        if (getGameState() == GameState.CRASHED)
            throw new RuntimeException("This instance have crashed an no longer usable.");
        if (getGameState() == GameState.UNINITIALIZED)
            throw new RuntimeException("Can not retry a game that hasn't ben initialized yet.");
        if (getGameState() == GameState.DISPOSED)
            throw new RuntimeException("Can not restart if the resources are disposed.");

        restart(fromCheckpoint);
    }

    public void exit() {
        shutdown = true;
    }

    public FutureObject<Exception> getException() {
        return exception;
    }

    /**
     * Events to execute at a given state. The event will be executed outside the OpenGL thread.
     *
     * @param gameState The state to listen to.
     * @param event     The event to execute.
     */
    public void setGameStateEvent(GameState gameState, Event event) {
        stateEvents.put(gameState, event);
    }

    private void setup() throws Exception {
        setGameState(GameState.LOADING);
        batch = new SpriteBatch();
        initCameras();
        level.init();
        level.build();
        level.insertDelete();

        if (isReplaying())
            level.processMeta(playback.meta);

        Dimension screenSize = getScreenSize();
        if (helpText == null)
            helpText = HUDMessage.centeredMessage("Can not pause in replay mode.", screenSize, Color.WHITE);
        if (deathText == null)
            deathText = HUDMessage.centeredMessage("You died. Press the quit or restart button to continue.", screenSize, Color.WHITE);
        if (deathCheckpointText == null)
            deathCheckpointText = HUDMessage.centeredMessage("You died. Press the quit or restart button to restart from latest checkpoint.", screenSize, Color.WHITE);
        if (pauseText == null)
            pauseText = HUDMessage.centeredMessage("Game is paused.", screenSize, Color.WHITE);
        if (winText == null)
            winText = HUDMessage.centeredMessage((isReplaying() ? "Replay done." :  "Congrats! You completed the level!") +
                   "\nPress the restart or quit button to continue.", screenSize, Color.WHITE);

        setGameState(GameState.ACTIVE);
    }

    private void restart(boolean checkpointPresent) {
        setGameState(GameState.LOADING);

        if(checkpointPresent && !level.cpPresent())
            checkpointPresent = false;

        if(completed())
            level.getCheckpointHandler().reset();

        if (lost() && checkpointPresent)
            deathCounter++;
        else if (completed())
            deathCounter = 0;

        if (!checkpointPresent) {
            time = 0;
        }

        uniqueCounter = 0;
        setZoom(1);
        setRotation(0);

        level.clean();
        level.build();
        level.insertDelete();

        frameCounter = 0;
        setGameState(GameState.ACTIVE);
    }

    private void overview() {
        if (shutdown)
            throw new ControlledException("Controlled termination.");

        if (!isReplaying()) {

            if ((active() || paused()) && keys(level.getAliveMainCharacters()).pause) {
                setGameState(paused() ? GameState.ACTIVE : GameState.PAUSED);

                if (active()) {
                    Music music = level.getStageMusic();
                    if (music != null)
                        music.setVolume(musicVolume);

                } else if (paused()) {
                    Music music = level.getStageMusic();
                    if (music != null) {
                        musicVolume = music.getVolume();
                        music.setVolume(.1f);
                    }
                }
            } else if (lost() || completed()) {
                Keystrokes keys = keys(level.getMainCharacters());

                if (keys.restart) {
                    restart(false);
                } else if (keys.quit) {
                    exit();
                }
            }
        } else {
            Keystrokes keys = keys(level.getMainCharacters());

            if (lost() && level.cpPresent() && !replayEnded()) {
                restart(true);
            } else if (completed() || lost()) {
                if (keys.restart) {
                    restart(false);
                } else if (keys.quit) {
                    exit();
                }
            }
        }

        if (paused()) {
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

    private void progress() {
        if (frameCounter++ > 2 && active()) //TODO: Why must frameCounter exceed 2?
            time += delta;

        prevTx = gameCamera.position.x;
        prevTy = gameCamera.position.y;

        level.gameLoop();
        statusControl();
    }

    private void paint() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        updateGameCamera();
        gameCamera();

        batch.begin();

        for (Entity entity : level.gameObjects)
            entity.render(batch);

        hudCamera();
        renderStatusBar();
        renderStatusText();

        batch.end();
    }

    private void setGameState(GameState state) {
        if (this.getGameState() == GameState.CRASHED)
            throw new IllegalStateException("This instance have crashed an no longer usable.");
        if (state == GameState.PAUSED && isReplaying())
            throw new IllegalArgumentException("Can not pause when playing a replay.");
        if (this.state == GameState.SUCCESS && (state == GameState.LOST || state == GameState.PAUSED))
            throw new IllegalArgumentException("Can not kill or pause a completed game.");

        if (state != this.getGameState()) {
            this.state = state;

            if (!isReplaying() && completed())
                finalizeRecording();

            Event event = stateEvents.get(this.state);
            if (event != null) {
                new Thread(event::eventHandling).start();
            }
        }
    }

    private void renderPause() {
        Gdx.gl.glClearColor(0, 0, 0, .4f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.begin();

        pauseText.draw(batch, timeFont);

        renderStatusBar();

        batch.end();
    }

    private void initCameras() {
        screenWidth = screenWidth == 0 ? Gdx.graphics.getWidth() : screenWidth;
        screenHeight = screenHeight == 0 ? Gdx.graphics.getHeight() : screenHeight;

        gameCamera = new OrthographicCamera();
        gameCamera.setToOrtho(flipY, screenWidth, screenHeight);

        hudCamera = new OrthographicCamera();
        hudCamera.setToOrtho(true, screenWidth, screenHeight);

        if (Gdx.graphics.getWidth() != screenWidth || Gdx.graphics.getHeight() != screenHeight)
            Gdx.graphics.setWindowedMode(screenWidth, screenHeight);
    }

    private void statusControl() {
        List<PlayableEntity> mains = level.getMainCharacters();
        int total = mains.size();
        long alive = mains.stream().filter(PlayableEntity::isAlive).count();
        long dead = mains.stream().filter(PlayableEntity::isDead).count();
        long finished = mains.stream().filter(PlayableEntity::isDone).count();

        if (dead == total || (alive == 0 && finished == 0))
            setGameState(GameState.LOST);
        else if (finished > 0)
            setGameState(GameState.SUCCESS);
        else if (alive > 0)
            setGameState(GameState.ACTIVE);
        else
            throw new IllegalStateException("Game is in a unknown state.");
    }

    long provideBadge() {
        return ++uniqueCounter;
    }

    boolean lost() {
        return getGameState() == GameState.LOST;
    }

    boolean completed() {
        return getGameState() == GameState.SUCCESS;
    }

    boolean paused() {
        return getGameState() == GameState.PAUSED;
    }

    boolean active() {
        return getGameState() == GameState.ACTIVE;
    }

    PlaybackRecord getPlayback() {
        return playback;
    }

    private boolean replayEnded() {
        List<PlayableEntity> mains = level.getMainCharacters();
        return mains.size() == mains.stream().filter(PlayableEntity::hasEnded).count();
    }

    private void finalizeRecording() {
        Replay recording = new Replay();
        recording.date = ZonedDateTime.now();
        recording.time = getTimeInSeconds();
        recording.levelName = level.getLevelName();
        recording.meta = level.getMeta();
        recording.deaths = getDeathCounter();
        recording.keystrokes = level.getMainCharacters().stream().map(play -> new KeySession(play.getReplayData(), play.getBadge())).collect(Collectors.toList());
        recordings.add(recording);
    }

    private void renderStatusBar() {
        timeFont.setColor(state == GameState.PAUSED ? Color.WHITE : timeColor);
        timeFont.draw(batch, getTimeInSeconds() + "", 10, 10);

        List<PlayableEntity> mains = level.getNonDeadMainCharacters();

        for (int index = 0, y = 40; index < mains.size(); index++) {
            PlayableEntity main = mains.get(index);
            int hp = main.getHP();

            if (main.healthHud != null && main.getState() != Vitality.DEAD && hp > 0) {
                Image2D healthHud = main.healthHud.getObject();
                final float width = healthHud.getWidth() + 3;

                for (int i = 0, posX = 10; i < hp; i++, posX += width)
                    batch.draw(healthHud, posX, y, healthHud.getWidth(), healthHud.getHeight(), 0, 0, healthHud.getWidth(), healthHud.getHeight(), false, true);

                y += healthHud.getHeight() + 3;
            }
        }
    }

    private void renderStatusText() {
        if (!renderText || timeFont == null)
            return;

        if (isReplaying() && keys(level.getAliveMainCharacters()).pause)
            showHelpText = !showHelpText;

        if (showHelpText && helpText != null)
            helpText.draw(batch, timeFont);

        if (completed() && winText != null) {
            winText.draw(batch, timeFont);
        } else if (lost() && deathText != null) {
            if(level.cpPresent())
                deathCheckpointText.draw(batch, timeFont);
            else
                deathText.draw(batch, timeFont);
        }
    }

    private Keystrokes keys(List<PlayableEntity> plays) {
        return plays
                .stream()
                .map(play -> Keystrokes.from(play.getController()))
                .reduce(Keystrokes::merge)
                .orElse(Keystrokes.AFK);
    }

    static ApplicationListener wrap(Engine engine) {
        return new ApplicationListener() {
            @Override
            public void dispose() {
                engine.destroy();
            }

            @Override
            public void render() {
                try {
                    engine.overview();
                } catch (ControlledException e) {
                    engine.setGameState(GameState.DISPOSED);
                    throw e;
                } catch (Exception e) {
                    engine.exception.set(e);
                    engine.setGameState(GameState.CRASHED);
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void create() {
                try {
                    engine.setup();
                } catch (Exception e) {
                    engine.exception.set(e);
                    engine.setGameState(GameState.CRASHED);
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void resize(int i, int i1) {
            }

            @Override
            public void pause() {
            }

            @Override
            public void resume() {
            }
        };
    }
}