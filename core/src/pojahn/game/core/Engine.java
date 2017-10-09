package pojahn.game.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Rectangle;
import pojahn.game.essentials.ControlledException;
import pojahn.game.essentials.GameState;
import pojahn.game.essentials.HUDMessage;
import pojahn.game.essentials.Image2D;
import pojahn.game.essentials.Keystrokes;
import pojahn.game.essentials.Vitality;
import pojahn.game.essentials.recording.PlaybackRecord;
import pojahn.game.essentials.recording.RecordingDevice;
import pojahn.game.essentials.recording.Replay;
import pojahn.game.essentials.shaders.DefaultShader;
import pojahn.game.events.Event;
import pojahn.lang.Obj;
import pojahn.lang.OtherMath;

import java.awt.*;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Vector;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * An instance of Engine is used to play one course.
 * To play a new course, dispose this one and create a new instance.
 */
public class Engine {

    public float delta = 1.0f / 60.0f;
    public boolean renderText;
    public BitmapFont timeFont;
    public Color timeColor;
    public HUDMessage helpText, winText, deathText, deathCheckpointText, pauseText;

    private final Level level;
    private final RecordingDevice device;
    private final Serializable meta;
    private final Executor eventExecutor;
    private String playerName;
    private SpriteBatch batch;
    private List<Replay> recordings;
    private OrthographicCamera gameCamera, hudCamera;
    private Map<GameState, Event> stateEvents;
    GameState state;
    Exception exception;

    private boolean replaying, flipY, showHelpText;
    private int screenWidth, screenHeight, deathCounter;
    private float rotation, musicVolume, prevTx, prevTy, time;
    private long frameCounter, uniqueCounter;

    public Engine(final Level level) {
        this(level, null);
    }

    public Engine(final Level level, final PlaybackRecord replayData) {
        state = GameState.UNINITIALIZED;
        this.level = Objects.requireNonNull(level);
        this.level.engine = this;

        device = new RecordingDevice();
        if (replayData != null) {
            device.load(replayData.replayData);
            meta = replayData.meta;
            replaying = true;
        } else {
            recordings = new Vector<>();
            meta = null;
        }

        eventExecutor = Executors.newSingleThreadExecutor();
        stateEvents = new HashMap<>();
        renderText = true;
        timeColor = Color.WHITE;
        flipY = true;
    }

    public List<Replay> getRecordings() {
        if (!isReplaying()) {
            final List<Replay> recordings = new ArrayList<>(this.recordings);
            this.recordings.clear();
            return recordings;
        }
        return Collections.emptyList();
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

    public void setPlayerName(final String playerName) {
        this.playerName = playerName;
    }

    public void setScreenSize(final int width, final int height) {
        screenWidth = width;
        screenHeight = height;
        initCameras();
    }

    public void setRotation(final float rotation) {
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

    public void setZoom(final float zoom) {
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

    public void translate(final float tx, final float ty) {
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

    public SpriteBatch getSpriteBatch() {
        return batch;
    }

    public void updateGameCamera() {
        gameCamera.update();
    }

    public boolean onScreen(final Entity entity) {
        final Rectangle bbox = BaseLogic.getBoundingBox(entity.bounds);
        return gameCamera.frustum.boundsInFrustum(bbox.x, bbox.y, 0, bbox.width / 2, bbox.height / 2, 0);
    }

    public void retry() {
        retry(level.cpPresent());
    }

    public void retry(final boolean fromCheckpoint) {
        if (isReplaying())
            throw new RuntimeException("Can not retry a replay.");
        if (getGameState() == GameState.CRASHED)
            throw new RuntimeException("This instance have crashed an no longer usable.");
        if (getGameState() == GameState.UNINITIALIZED)
            throw new RuntimeException("Can not retry a game that hasn't ben initialized yet.");
        if (getGameState() == GameState.DISPOSED)
            throw new RuntimeException("Can not restart if the resources are disposed.");

        Gdx.app.postRunnable(() -> restart(fromCheckpoint));
    }

    public void exit() {
        Gdx.app.postRunnable(() -> {
            throw new ControlledException("Controlled termination.");
        });
    }

    public Exception getException() {
        return exception;
    }

    /**
     * Events to execute at a given state. The event will be executed outside the OpenGL thread.
     *
     * @param gameState The state to listen to.
     * @param event     The event to execute.
     */
    public void setGameStateEvent(final GameState gameState, final Event event) {
        stateEvents.put(gameState, event);
    }

    void setup() throws Exception {
        setGameState(GameState.LOADING);
        batch = new SpriteBatch(1000, new DefaultShader().get());
        ShaderProgram.pedantic = false;
        initCameras();
        level.init(meta);
        level.build();
        level.place();

        final Dimension screenSize = getScreenSize();
        helpText = Obj.nonNull(helpText, HUDMessage.centeredMessage("Can not pause in replay mode.", screenSize, Color.WHITE));
        deathText = Obj.nonNull(deathText, HUDMessage.centeredMessage("You died. Press the quit or restart button to continue.", screenSize, Color.WHITE));
        deathCheckpointText = Obj.nonNull(deathCheckpointText, HUDMessage.centeredMessage("You died.\nPress the quit or restart button\nto restart from latest checkpoint.", screenSize, Color.WHITE));
        pauseText = Obj.nonNull(pauseText, HUDMessage.centeredMessage("Game is paused.", screenSize, Color.WHITE));
        winText = Obj.nonNull(winText, HUDMessage.centeredMessage((isReplaying() ? "Replay done." : "Congrats! You completed the level!") +
                "\nPress the restart or quit button to continue.", screenSize, Color.WHITE));

        setGameState(GameState.ACTIVE);
    }

    private void restart(boolean fromCp) {
        final boolean lost = lost();
        final boolean completed = completed();
        setGameState(GameState.LOADING);

        rotation = 0;
        flipY = true;
        initCameras();

        fromCp = fromCp && level.cpPresent() && !completed;
        time = !fromCp ? 0 : time;
        uniqueCounter = frameCounter = 0;

        if (completed || lost && !fromCp) {
            if (isReplaying())
                device.reset();
            else
                device.clear();
        }

        if (lost && fromCp) {
            deathCounter++;
        } else if (completed) {
            deathCounter = 0;
            level.getCheckpointHandler().reset();
        }

        level.clean();
        level.build();
        level.place();

        if (lost && fromCp) {
            level.getCheckpointHandler().placeUsers();
        }

        setGameState(GameState.ACTIVE);
    }

    void overview() {
        if (!isReplaying()) {
            if ((active() || paused()) && keys(level.getAliveMainCharacters()).pause) {
                setGameState(paused() ? GameState.ACTIVE : GameState.PAUSED);
                adjustMusic();
            } else if (lost() || completed()) {
                final Keystrokes keys = keys(level.getMainCharacters());
                if (keys.restart) {
                    restart(lost() && level.cpPresent());
                } else if (keys.quit) {
                    exit();
                }
            }
        } else {
            if (lost() && level.cpPresent() && !device.allDone()) {
                restart(true);
            } else if (completed() || lost()) {
                final Keystrokes keys = keys(level.getMainCharacters());
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

    private void adjustMusic() {
        if (active()) {
            final Music music = level.getStageMusic();
            if (music != null)
                music.setVolume(musicVolume);

        } else if (paused()) {
            final Music music = level.getStageMusic();
            if (music != null) {
                musicVolume = music.getVolume();
                music.setVolume(.1f);
            }
        }
    }

    void destroy() {
        if (batch != null)
            batch.dispose();
        if (level != null)
            level.dispose();
        batch = null;
    }

    private void progress() {
        if (active())
            time += delta;

        frameCounter++;
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

        level.gameObjects.forEach(entity -> entity.render(batch));

        hudCamera();
        renderStatusBar();
        renderStatusText();

        batch.end();
    }

    void setGameState(final GameState state) {
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

            runStateEvent();
        }
    }

    void runStateEvent() {
        final Event event = stateEvents.get(this.state);
        if (event != null) {
            eventExecutor.execute(event::eventHandling);
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
        if (getGameState() == GameState.ACTIVE) {
            final List<PlayableEntity> mains = level.getMainCharacters();
            final int total = mains.size();
            final long alive = mains.stream().filter(PlayableEntity::isAlive).count();
            final long dead = mains.stream().filter(PlayableEntity::isDead).count();
            final long finished = mains.stream().filter(PlayableEntity::isDone).count();

            if (dead == total || (alive == 0 && finished == 0))
                setGameState(GameState.LOST);
            else if (finished > 0)
                setGameState(GameState.SUCCESS);
            else if (alive > 0)
                setGameState(GameState.ACTIVE);
            else
                throw new IllegalStateException("Game is in a unknown state.");
        }
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

    RecordingDevice getDevice() {
        return device;
    }

    private void finalizeRecording() {
        final Replay recording = new Replay();
        recording.date = ZonedDateTime.now();
        recording.time = getTimeInSeconds();
        recording.levelName = level.getLevelName();
        recording.meta = level.getMeta();
        recording.deaths = getDeathCounter();
        recording.outcome = getGameState();
        recording.keystrokes = device.export();
        recording.playerName = playerName;
        recordings.add(recording);
    }

    private void renderStatusBar() {
        timeFont.setColor(state == GameState.PAUSED ? Color.WHITE : timeColor);
        timeFont.draw(batch, getTimeInSeconds() + "", 10, 10);

        final List<PlayableEntity> mains = level.getNonDeadMainCharacters();

        for (int index = 0, y = 40; index < mains.size(); index++) {
            final PlayableEntity main = mains.get(index);
            final int hp = main.getHP();

            if (main.healthHud != null && main.getState() != Vitality.DEAD && hp > 0) {
                final Image2D healthHud = main.healthHud.getObject();
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
            if (level.cpPresent())
                deathCheckpointText.draw(batch, timeFont);
            else
                deathText.draw(batch, timeFont);
        }
    }

    private Keystrokes keys(final List<PlayableEntity> plays) {
        return plays
                .stream()
                .map(play -> Keystrokes.from(play.getController()))
                .reduce(Keystrokes::merge)
                .orElse(Keystrokes.AFK);
    }
}