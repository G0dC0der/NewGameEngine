package pojahn.game.desktop.redguyruns.gui;

import com.badlogic.gdx.files.FileHandle;
import pojahn.game.core.Engine;
import pojahn.game.core.Level;
import pojahn.game.desktop.GameLauncher;
import pojahn.game.desktop.redguyruns.logic.SaveState;
import pojahn.game.desktop.redguyruns.logic.World;
import pojahn.game.desktop.redguyruns.util.GameUtil;
import pojahn.game.essentials.GameState;
import pojahn.lang.IO;

import javax.swing.*;
import java.io.File;
import java.io.IOException;

public class MainClass {

    public static void main(final String... args) throws Exception {
        if (!new File("savestate.sav").exists()) {
            createSaveState();
        }

        final SaveState saveState = (SaveState) IO.importObject(new FileHandle("savestate.sav"));
        final Level level = World.LEVELS.get(saveState.level).getDeclaredConstructor().newInstance();

        JOptionPane.showMessageDialog(null, "Save Name: " + saveState.name + "\nLevel: " + level.getLevelName());

        final Engine engine = new Engine(level);
        engine.setPlayerName(saveState.name);
        engine.setGameStateEvent(GameState.SUCCESS, () -> {
            GameUtil.exportReplays(engine, GameState.SUCCESS).eventHandling();
            levelComplete(saveState);
        });
        GameLauncher.launch(engine, GameUtil.getBasicConfig());
    }

    private static void createSaveState() throws IOException {
        final String saveName = JOptionPane.showInputDialog("Enter the name of the save file");
        final SaveState saveState = new SaveState();
        saveState.name = saveName;
        saveState.level = 0;

        IO.exportObject(saveState, new File("savestate.sav"));
    }

    private static void levelComplete(final SaveState saveState) {
        saveState.level++;

        try {
            IO.exportObject(saveState, new File("savestate.sav"));
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }
}
