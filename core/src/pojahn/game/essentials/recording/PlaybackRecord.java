package pojahn.game.essentials.recording;

import com.google.common.collect.ImmutableList;

import java.io.Serializable;
import java.util.List;

public class PlaybackRecord {

    public final List<KeySession> replayData;
    public final Serializable meta;

    public PlaybackRecord(final List<KeySession> replayData, final Serializable meta) {
        this.replayData = ImmutableList.copyOf(replayData);
        this.meta = meta;
    }
}
