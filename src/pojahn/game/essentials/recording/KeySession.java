package pojahn.game.essentials.recording;

import pojahn.game.essentials.Keystrokes;

import java.io.Serializable;
import java.util.List;

public class KeySession implements Serializable {

    public final List<Keystrokes> keystrokes;
    public final long badge;

    public KeySession(List<Keystrokes> keystrokes, long badge) {
        this.keystrokes = keystrokes;
        this.badge = badge;
    }
}
