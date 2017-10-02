package pojahn.game.core;

import com.badlogic.gdx.ApplicationListener;
import pojahn.game.essentials.ControlledException;
import pojahn.game.essentials.GameState;

import java.util.Objects;

public class GameContainer implements ApplicationListener {

    private final Engine engine;

    public GameContainer(final Engine engine) {
        this.engine = Objects.requireNonNull(engine);
    }

    private void forwardError(final Exception e) {
        if (engine.exception != null) {
            e.initCause(engine.exception);
        }
        engine.exception = e;
        engine.state = GameState.CRASHED;
        engine.runStateEvent();
        throw new RuntimeException(e);
    }

    @Override
    public void dispose() {
        try {
            engine.destroy();
        } catch (final Exception e) {
            forwardError(e);
        }
    }

    @Override
    public void render() {
        try {
            engine.overview();
        } catch (final ControlledException e) {
            engine.setGameState(GameState.DISPOSED);
            throw e;
        } catch (final Exception e) {
            forwardError(e);
        }
    }

    @Override
    public void create() {
        try {
            engine.setup();
        } catch (final Exception e) {
            forwardError(e);
        }
    }

    @Override
    public void resize(final int i, final int i1) {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }
}
