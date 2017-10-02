package pojahn.game.entities;

import com.badlogic.gdx.math.Vector2;
import pojahn.game.core.Collisions;
import pojahn.game.core.MobileEntity;
import pojahn.game.events.Event;

import java.util.ArrayList;
import java.util.List;

public class Shuttle extends MobileEntity {

    private static class Waypoint {
        Vector2 target;
        Event event;

        Waypoint(final float x, final float y, final Event event) {
            this.target = new Vector2(x, y);
            this.event = event;
        }
    }

    public float thrust, drag, delta, vx, vy;
    private int counter;
    private Vector2 waypointDirection;
    private List<Waypoint> waypoints;

    public Shuttle(final float x, final float y) {
        move(x, y);
        this.waypoints = new ArrayList<>();
    }

    @Override
    public void init() {
        thrust = 500f;
        drag = .5f;
        delta = getEngine().delta;
    }

    public void appendPath() {
        appendPath(x(), y());
    }

    public void appendPath(final float x, final float y) {
        appendPath(x, y, null);
    }

    public void appendPath(final Vector2 path) {
        appendPath(path.x, path.y);
    }

    public void appendPath(final Vector2[] paths) {
        for (final Vector2 path : paths)
            appendPath(path.x, path.y);
    }

    public void appendPath(final float x, final float y, final Event reachEvent) {
        waypoints.add(new Waypoint(x, y, reachEvent));
    }

    public void clearWaypoints() {
        waypoints.clear();
    }

    public void nextWaypoint() {
        counter = ++counter % waypoints.size();
    }

    public void firstWaypoint() {
        counter = 0;
    }

    public void lastWaypoint() {
        counter = waypoints.size() - 1;
    }

    @Override
    @Deprecated
    public void setMoveSpeed(final float moveSpeed) {
        throw new UnsupportedOperationException("The Shuttle class use maxX, maxY, accX and accY to control the speed.");
    }

    @Override
    public void logistics() {
        if (!isFrozen() && !waypoints.isEmpty()) {
            final Waypoint wp = waypoints.get(counter);

            if (x() == wp.target.x && y() == wp.target.y)    //Make sure we don't get NaN when normalizing.
                bounds.pos.x--;

            final Vector2 currentDirection = Collisions.normalize(bounds.pos.x, bounds.pos.y, wp.target.x, wp.target.y);

            if (waypointDirection == null)
                waypointDirection = currentDirection.cpy();

            if (waypointDirection.dot(currentDirection) < 0) {
                counter = ++counter % waypoints.size();
                waypointDirection = null;

                if (wp.event != null)
                    wp.event.eventHandling();
            } else {
                final float accX = thrust * -currentDirection.x - drag * vx;
                final float accY = thrust * -currentDirection.y - drag * vy;

                vx += delta * accX;
                vy += delta * accY;

                bounds.pos.x += delta * vx;
                bounds.pos.y += delta * vy;
            }
        }
    }
}