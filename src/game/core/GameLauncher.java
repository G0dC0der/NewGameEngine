package game.core;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

import game.core.Engine.GDXApp;

public class GameLauncher {
	
	public static synchronized void launchLWJGLGame(Engine engine, LwjglApplicationConfiguration cfg){
		GDXApp app = new GDXApp();
		app.engine = engine;
		
		new LwjglApplication(app, cfg);
	}
}
