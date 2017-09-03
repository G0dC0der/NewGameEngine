package pojahn.game.desktop.redguyruns.logic;

import com.badlogic.gdx.Gdx;
import pojahn.lang.IO;

import java.io.IOException;

public class Controllers {

    public static final Controllers P1_CONTROLLER;
    public static final Controllers P2_CONTROLLER;

    static {
        try {
            P1_CONTROLLER = (Controllers) IO.importObject(Gdx.files.internal("res/data/controller1.con"));
            P2_CONTROLLER = (Controllers) IO.importObject(Gdx.files.internal("res/data/controller2.con"));
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
