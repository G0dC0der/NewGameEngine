package pojahn.game.desktop.redguyruns.gui;

import pojahn.game.core.Engine;
import pojahn.game.desktop.GameLauncher;
import pojahn.game.desktop.redguyruns.levels.sand.Sandopolis;
import pojahn.game.desktop.redguyruns.util.GameUtil;
import pojahn.game.essentials.GameState;
import pojahn.game.essentials.recording.PlaybackRecord;

import java.io.IOException;

public class MainClass {

    public static void main(String... args) throws IOException, ClassNotFoundException {
        PlaybackRecord record = null;
//        Replay replay = (Replay) IO.importObjectCompressed(new FileHandle(System.getProperty("user.dir") + "\\replays\\In A Hurry 34.8 SUCCESS.rlp"));
//        record = new PlaybackRecord(replay.keystrokes, replay.meta);

        Engine engine = new Engine(new Sandopolis(), record);
        engine.setGameStateEvent(GameState.SUCCESS, GameUtil.exportForGhostData(engine));
        GameLauncher.launch(engine, GameUtil.getBasicConfig());
    }
}
