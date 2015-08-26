package pojahn.game.core;

import pojahn.game.core.Engine.GDXApp;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

public class GameLauncher {
	
	public static void launchGame(Engine engine, LwjglApplicationConfiguration cfg){
		GDXApp app = new GDXApp();
		app.engine = engine;
		
		new LwjglApplication(app, cfg);
	}
}
