package pojahn.game.essentials;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import pojahn.game.core.Collisions;
import pojahn.game.core.Entity;
import pojahn.game.events.Event;

import java.util.ArrayList;

public class CheckPointHandler {

    private static class Checkpoint {
        float startX, startY, x, y, width, height;
        boolean taken;

        Checkpoint(final float startX, final float startY, final float x, final float y, final float width, final float height) {
            super();
            this.startX = startX;
            this.startY = startY;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
    }

    private ArrayList<Checkpoint> checkpoints;
    private ArrayList<Entity> users;
    private Event reachEvent;

    public CheckPointHandler() {
        checkpoints = new ArrayList<>();
        users = new ArrayList<>();
    }

    public void addUser(final Entity user) {
        users.add(user);
    }

    public void clearUsers() {
        users.clear();
    }

    public void appendCheckpoint(final Vector2 startPos, final Rectangle area) {
        appendCheckpoint(startPos.x, startPos.y, area.x, area.y, area.width, area.height);
    }

    public void appendCheckpoint(final float startX, final float startY, final Rectangle area) {
        appendCheckpoint(startX, startY, area.x, area.y, area.width, area.height);
    }

    public void appendCheckpoint(final Vector2 startPos, final float x, final float y, final float width, final float height) {
        appendCheckpoint(startPos.x, startPos.y, x, y, width, height);
    }

    public void appendCheckpoint(final float startX, final float startY, final float x, final float y, final float width, final float height) {
        checkpoints.add(new Checkpoint(startX, startY, x, y, width, height));
    }

    public void reset() {
        checkpoints.forEach(cp -> cp.taken = false);
    }

    public boolean available() {
        return getLatestCheckpoint() != null;
    }

    public Vector2 getLatestCheckpoint() {
        for (int i = checkpoints.size() - 1; i >= 0; i--) {
            final Checkpoint cp = checkpoints.get(i);

            if (cp.taken)
                return new Vector2(cp.startX, cp.startY);
        }

        return null;
    }

    public boolean reached(final int cpIndex) {
        return checkpoints.get(cpIndex).taken;
    }

    public void setReachEvent(final Event reachEvent) {
        this.reachEvent = reachEvent;
    }

    public void placeUsers() {
        final Vector2 latest = getLatestCheckpoint();
        float width = 0;
        if (latest != null) {
            for (int i = 0; i < users.size(); i++) {
                if (i > 0)
                    width += users.get(i - 1).width();
                users.get(i).move(latest.x + width, latest.y);
            }
        }
    }

    public void update() {
        Outer:
        for (final Checkpoint cp : checkpoints) {
            if (!cp.taken) {
                for (final Entity user : users) {
                    if (Collisions.rectanglesCollide(user.x(), user.y(), user.width(), user.height(), cp.x, cp.y, cp.width, cp.height)) {
                        cp.taken = true;
                        if (reachEvent != null)
                            reachEvent.eventHandling();

                        continue Outer;
                    }
                }
            }
        }
    }
}