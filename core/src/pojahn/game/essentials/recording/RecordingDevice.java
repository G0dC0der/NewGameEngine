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

    public void addEntry(final String identifier) {
        recordEntries.add(new KeySession(new LinkedList<>(), identifier));
    }

    public void addFrame(final String identifier, final Keystrokes keystrokes) {
        for (final KeySession recordEntry : recordEntries) {
            if (recordEntry.identifier.equals(identifier)) {
                recordEntry.keystrokes.add(keystrokes);
                return;
            }
        }
    }

    public Keystrokes nextInput(final String identifier) {
        for (final KeySession recordEntry : recordEntries) {
            if (recordEntry.identifier.equals(identifier)) {
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
