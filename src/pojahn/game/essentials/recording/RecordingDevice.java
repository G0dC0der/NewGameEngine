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

    public void addEntry(long badge) {
        recordEntries.add(new KeySession(new LinkedList<>(), badge));
    }

    public void addFrame(long badge, Keystrokes keystrokes) {
        for (KeySession recordEntry : recordEntries) {
            if (recordEntry.badge == badge) {
                recordEntry.keystrokes.add(keystrokes);
                return;
            }
        }
    }

    public Keystrokes nextInput(long badge) {
        for (KeySession recordEntry : recordEntries) {
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

    public void load(List<KeySession> recordEntries) {
        this.recordEntries = recordEntries;
    }

    public void clear() {
        recordEntries = new ArrayList<>();
    }

    public boolean allDone () {
        for (KeySession recordEntry : recordEntries) {
            if (!recordEntry.hasEnded()) {
                return false;
            }
        }
        return true;
    }
}
