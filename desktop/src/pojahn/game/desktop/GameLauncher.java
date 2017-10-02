package pojahn.game.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import pojahn.game.core.Engine;
import pojahn.game.core.GameContainer;
import pojahn.game.essentials.ControlledException;

public class GameLauncher {

    public static void launch(final Engine engine, final Lwjgl3ApplicationConfiguration cfg) {
        new Thread(() -> {
            try {
                new Lwjgl3Application(new GameContainer(engine), cfg);
            } catch (ControlledException e) {
                System.out.println("Exiting game.");
            }
        }).start();
    }
}
