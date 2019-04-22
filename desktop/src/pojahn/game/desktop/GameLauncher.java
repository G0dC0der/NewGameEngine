package pojahn.game.desktop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import pojahn.game.core.Engine;
import pojahn.game.core.GameContainer;
import pojahn.game.essentials.ControlledException;

public class GameLauncher {

    public static void launch(final Engine engine, final LwjglApplicationConfiguration cfg) {
        try {
            new LwjglApplication(new GameContainer(engine), cfg);
        } catch (final ControlledException e) {
            System.out.println("Exiting game.");
            Gdx.app.exit();
        }
    }
}
