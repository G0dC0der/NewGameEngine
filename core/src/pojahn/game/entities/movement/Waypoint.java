package pojahn.game.entities.movement;

import pojahn.game.events.Event;

public class Waypoint implements java.io.Serializable {

    public final float targetX, targetY;
    public final int frames;
    public final boolean jump;
    public final Event event;

    public Waypoint(final float targetX, final float targetY, final int frames, final boolean jump, final Event event) {
        this.targetX = targetX;
        this.targetY = targetY;
        this.frames = frames;
        this.jump = jump;
        this.event = event;
    }

    public Waypoint(final float targetX, final float targetY) {
        this(targetX, targetY, 0, false, null);
    }

    @Override
    public String toString() {
        return targetX + ":" + targetY + " " + frames + " - " + jump;
    }
}
