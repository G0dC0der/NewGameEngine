package pojahn.game.core;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import pojahn.game.essentials.ControlledException;

public class GameLauncher {

    public static void launch(Engine engine, Lwjgl3ApplicationConfiguration cfg) {
        new Thread(()->{
            try {
                new Lwjgl3Application(Engine.wrap(engine), cfg);
            } catch (ControlledException e) {
                System.out.println("Exiting game.");
            }
        }).start();
    }
}
