package pojahn.game.essentials.recording;

import pojahn.game.essentials.Keystrokes;

import java.io.Serializable;
import java.util.List;

public class PlaybackRecord {

    public final List<KeySession> replayData;
    public final Serializable meta;

    public PlaybackRecord(final List<KeySession> replayData, final Serializable meta) {
        this.replayData = replayData;
        this.meta = meta;
    }

    public List<Keystrokes> getByBadge(final long badge) {
        return replayData.stream()
                .filter(keySession -> keySession.badge == badge)
                .findFirst()
                .get().keystrokes;
    }
}
