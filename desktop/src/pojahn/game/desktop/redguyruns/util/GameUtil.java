package pojahn.game.desktop.redguyruns.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import pojahn.game.core.Engine;
import pojahn.game.essentials.recording.Replay;
import pojahn.game.events.Event;
import pojahn.lang.IO;

import java.io.IOException;
import java.util.List;

public class GameUtil {

    public static Lwjgl3ApplicationConfiguration getBasicConfig() {
        Lwjgl3ApplicationConfiguration cfg = new Lwjgl3ApplicationConfiguration();
        cfg.useOpenGL3(true, 3 ,2);
        cfg.setWindowedMode(800, 600);
        cfg.setTitle("Red Guy Runs");
        cfg.setResizable(false);

        return cfg;
    }

    public static Event exportReplays(Engine engine) {
        return () -> {
            List<Replay> replays = engine.getRecordings();
            if (replays != null) {
                for (Replay replay : replays) {
                    try {
                        IO.exportObjectCompressed(replay, Gdx.files.internal("replays/" + String.format("%s %s %s.rlp", replay.levelName, replay.time, replay.outcome)));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
    }

    public static Event exportForGhostData(Engine engine) {
        return () -> {
            List<Replay> replays = engine.getRecordings();
            if (replays != null && !replays.isEmpty()) {
                Replay r = replays.get(0);
                try {
                    IO.exportObject(r.keystrokes.get(0), Gdx.files.internal("replays/ghost-" + r.levelName + ".rlp"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
    }
}
