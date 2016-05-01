package pojahn.game.essentials;

import pojahn.game.core.PlayableEntity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PlaybackRecord {

    private List<Keystrokes.KeystrokeSession> replayData;
    private Serializable meta;

    public PlaybackRecord(){

    }

    public PlaybackRecord(List<List<Keystrokes>> replayData, Serializable meta) {
        this.replayData = new ArrayList<>(replayData.size());
        replayData.forEach(el -> this.replayData.add(Keystrokes.KeystrokeSession.from(el)));
        this.meta = meta;
    }

    public boolean hasEnded() {
        for(Keystrokes.KeystrokeSession ks : replayData)
            if(!ks.hasEnded())
                return false;
        return true;
    }

    public static PlaybackRecord from(Replay replay) {
        PlaybackRecord pbd = new PlaybackRecord();
        pbd.replayData = replay.keystrokes;
        pbd.meta = replay.meta;

        return pbd;
    }
}
