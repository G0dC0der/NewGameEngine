package pojahn.game.desktop.redguyruns.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.files.FileHandle;
import pojahn.game.core.Engine;
import pojahn.game.essentials.GameState;
import pojahn.game.essentials.recording.Replay;
import pojahn.game.events.Event;
import pojahn.lang.IO;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class GameUtil {

    public static LwjglApplicationConfiguration getBasicConfig() {
        final LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
        cfg.resizable = false;
        cfg.useGL30 = true;
        cfg.width = 800;
        cfg.height = 600;
        cfg.title = "Red Guy Runs";

        return cfg;
    }

    public static Event exportReplays(final Engine engine, final GameState replayOutcome) {
        return () -> {
            engine.getRecordings()
                .stream()
                .filter(replay -> replay.outcome == replayOutcome)
                .forEach(replay -> {
                    try {
                        final File replayDir = new File("replays");
                        replayDir.mkdirs();

                        final File exportFile = new File(replayDir, String.format("%s %s %s.rlp", replay.levelName, replay.time, replay.outcome));
                        IO.exportObject(replay, exportFile);
                    } catch (final IOException e) {
                        e.printStackTrace();
                    }
                });
        };
    }

    public static Event exportForGhostData(final Engine engine) {
        return () -> {
            engine.getRecordings().forEach(replay -> {
                try {
                    IO.exportObject(replay.keystrokes.get(0), new File("ghost-" + replay.levelName + ".rlp"));
                } catch (final IOException e) {
                    e.printStackTrace();
                }
            });
        };
    }
}
