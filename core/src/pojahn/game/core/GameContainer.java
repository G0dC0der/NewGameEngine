package pojahn.game.core;

import com.badlogic.gdx.ApplicationListener;
import pojahn.game.essentials.ControlledException;
import pojahn.game.essentials.GameState;

import java.util.Objects;

public class GameContainer implements ApplicationListener {

    private final Engine engine;

    public GameContainer(Engine engine) {
        this.engine = Objects.requireNonNull(engine);
    }

    private void forwardError(Exception e) {
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
        } catch (Exception e) {
            forwardError(e);
        }
    }

    @Override
    public void render() {
        try {
            engine.overview();
        } catch (ControlledException e) {
            engine.setGameState(GameState.DISPOSED);
            throw e;
        } catch (Exception e) {
            forwardError(e);
        }
    }

    @Override
    public void create() {
        try {
            engine.setup();
        } catch (Exception e) {
            forwardError(e);
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
}
