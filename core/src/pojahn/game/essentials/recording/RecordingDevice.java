package pojahn.game.essentials.recording;

import pojahn.game.essentials.Keystrokes;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class RecordingDevice {

    private List<KeySession> recordEntries;

    public RecordingDevice() {
        recordEntries = new ArrayList<>();
    }

    public void addEntry(final long badge) {
        recordEntries.add(new KeySession(new LinkedList<>(), badge));
    }

    public void addFrame(final long badge, final Keystrokes keystrokes) {
        for (final KeySession recordEntry : recordEntries) {
            if (recordEntry.badge == badge) {
                recordEntry.keystrokes.add(keystrokes);
                return;
            }
        }
    }

    public Keystrokes nextInput(final long badge) {
        for (final KeySession recordEntry : recordEntries) {
            if (recordEntry.badge == badge) {
                return recordEntry.nextInput();
            }
        }
        throw new IllegalArgumentException("The badge did not map to a recording entry.");
    }

    public void reset() {
        recordEntries.forEach(KeySession::reset);
    }

    public List<KeySession> export() {
        return recordEntries;
    }

    public void load(final List<KeySession> recordEntries) {
        this.recordEntries = recordEntries;
    }

    public void clear() {
        recordEntries = new ArrayList<>();
    }

    public boolean allDone() {
        for (final KeySession recordEntry : recordEntries) {
            if (!recordEntry.hasEnded()) {
                return false;
            }
        }
        return true;
    }
}
