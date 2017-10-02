package pojahn.game.essentials.recording;

import pojahn.game.essentials.Keystrokes;

import java.io.Serializable;
import java.util.List;

public class KeySession implements Serializable {

    private static final long serialVersionUID = -1L;

    public final List<Keystrokes> keystrokes;
    final long badge;
    private transient int index;

    KeySession(final List<Keystrokes> keystrokes, final long badge) {
        this.keystrokes = keystrokes;
        this.badge = badge;
    }

    public boolean hasEnded() {
        return index > keystrokes.size() - 1;
    }

    public Keystrokes nextInput() {
        return hasEnded() ? Keystrokes.AFK : keystrokes.get(index++);
    }

    public void reset() {
        index = 0;
    }
}
