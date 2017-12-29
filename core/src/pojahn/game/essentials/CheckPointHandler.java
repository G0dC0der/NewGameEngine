package pojahn.game.essentials;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.google.common.collect.Lists;
import pojahn.game.core.Entity;
import pojahn.game.events.Event;

import java.util.ArrayList;

public class CheckPointHandler {

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
        checkpoints.add(new Checkpoint.AreaCheckpoint(startX, startY, x, y, width, height));
    }

    public void appendCheckpoint(final Checkpoint checkpoint) {
        checkpoints.add(checkpoint);
    }

    public void reset() {
        checkpoints.forEach(Checkpoint::reset);
    }

    public boolean available() {
        return getLatestCheckpoint() != null;
    }

    public Vector2 getLatestCheckpoint() {
        return Lists.reverse(checkpoints)
            .stream()
            .filter(Checkpoint::isTaken)
            .map(Checkpoint::getStart)
            .findFirst()
            .orElse(null);
    }

    public boolean reached(final int cpIndex) {
        return checkpoints.get(cpIndex).isTaken();
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
            if (!cp.isTaken()) {
                for (final Entity user : users) {
                    if (cp.reached(user)) {
                        cp.take();
                        if (reachEvent != null)
                            reachEvent.eventHandling();

                        continue Outer;
                    }
                }
            }
        }
    }
}