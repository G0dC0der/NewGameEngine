package pojahn.game.desktop.redguyruns.gui;

import pojahn.game.core.Engine;
import pojahn.game.desktop.GameLauncher;
import pojahn.game.desktop.redguyruns.levels.mutant.MutantLab;
import pojahn.game.desktop.redguyruns.levels.orbit.OrbitalStation;
import pojahn.game.desktop.redguyruns.levels.shadow.ShadowSection;
import pojahn.game.desktop.redguyruns.levels.sprit.SpiritTemple;
import pojahn.game.desktop.redguyruns.levels.stress.StressLevel;
import pojahn.game.desktop.redguyruns.levels.training1.TrainingStage1;
import pojahn.game.desktop.redguyruns.levels.training2.TrainingStage2;
import pojahn.game.desktop.redguyruns.levels.training3.TrainingStage3;
import pojahn.game.desktop.redguyruns.util.GameUtil;
import pojahn.game.essentials.GameState;
import pojahn.game.essentials.recording.PlaybackRecord;

import java.io.IOException;

public class MainClass {

    public static void main(final String... args) throws IOException, ClassNotFoundException {
        final PlaybackRecord record = null;
//        Replay replay = (Replay) IO.importObjectCompressed(new FileHandle(System.getProperty("user.dir") + "\\replays\\In A Hurry 34.8 SUCCESS.rlp"));
//        record = new PlaybackRecord(replay.keystrokes, replay.meta);

        final Engine engine = new Engine(new OrbitalStation(), record);
        engine.setGameStateEvent(GameState.SUCCESS, GameUtil.exportForGhostData(engine));
        GameLauncher.launch(engine, GameUtil.getBasicConfig());
    }
}
