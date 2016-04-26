package pojahn.game.core;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

public class GameLauncher {
	
	public static void launchGame(Engine engine, Lwjgl3ApplicationConfiguration cfg){
		new Lwjgl3Application(Engine.wrap(engine), cfg);
	}
}
