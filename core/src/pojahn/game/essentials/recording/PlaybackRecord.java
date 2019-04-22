package pojahn.game.essentials.recording;

import java.io.Serializable;
import java.util.List;

public class PlaybackRecord {

    public final List<KeySession> replayData;
    public final Serializable meta;

    public PlaybackRecord(final List<KeySession> replayData, final Serializable meta) {
        this.replayData = List.copyOf(replayData);
        this.meta = meta;
    }
}
