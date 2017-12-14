package pojahn.game.entities.movement;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import pojahn.game.core.BaseLogic;
import pojahn.game.core.MobileEntity;
import pojahn.game.essentials.GravityAware;
import pojahn.game.events.Event;
import pojahn.lang.Obj;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class EarthDrone extends MobileEntity {

    public enum ReachStrategy {
        SOFT,
        STRICT
    }

    private final List<Waypoint> waypoints;
    private final GravityAware gravityAware;
    private ReachStrategy reachStrategy;
    private Vector2 waypointDirection;
    private int waypointIndex, chillFrames;
    private float reachTolerance;

    public EarthDrone(final float x, final float y) {
        move(x, y);
        waypoints = new ArrayList<>();
        gravityAware = new GravityAware(this);
        gravityAware.maxY = -300;
        gravityAware.gravity = -1000;
        reachTolerance = 3f;
        reachStrategy = ReachStrategy.SOFT;
    }

    public void setReachStrategy(final ReachStrategy reachStrategy) {
        this.reachStrategy = Objects.requireNonNull(reachStrategy);
    }

    public void setReachTolerance(final float reachTolerance) {
        this.reachTolerance = reachTolerance;
    }

    public GravityAware getGravityAware() {
        return gravityAware;
    }

    public void deaccelerate() {
        gravityAware.velocity.x = gravityAware.velocity.y = 0;
    }

    public void addPath() {
        waypoints.add(new Waypoint.StaticWaypoint(x(), y()));
    }

    public void addPath(final float x, final float y) {
        waypoints.add(new Waypoint.StaticWaypoint(x, y));
    }

    public void addPath(final float x, final float y, final Event event) {
        waypoints.add(new Waypoint.StaticWaypoint(x, y, 0, false, event));
    }

    public void addPath(final  float x, final float y, final int chillFrames, final boolean jump, final Event event) {
        waypoints.add(new Waypoint.StaticWaypoint(x, y, chillFrames, jump, event));
    }

    public void addPath(final Waypoint waypoint) {
        waypoints.add(waypoint);
    }

    public void addReversed() {
        final List<Waypoint> reversed = new ArrayList<>(waypoints);
        reversed.remove(reversed.size() - 1);
        Collections.reverse(reversed);

        waypoints.addAll(reversed);
    }

    public void clear() {
        waypoints.clear();
    }

    @Override
    public void logistics() {
        if (isFrozen() || waypoints.isEmpty()) {
            return;
        }

        if (--chillFrames > 0) {
            gravityAware.glide();
            gravityAware.land();
            return;
        }

        final Waypoint waypoint = waypoints.get(waypointIndex);

        if (waypoint.targetX() > x()) {
            gravityAware.moveRight();
        } else if (waypoint.targetX() < x()){
            gravityAware.moveLeft();
        }
        if (waypoint.getTargetY() > y()) {
            gravityAware.drag();
        } else if (waypoint.getTargetY() < y()) {
            gravityAware.pull();
        }

        if (x() == waypoint.targetX() && y() == waypoint.getTargetY()) {
            reach(waypoint);
        } else if (reachStrategy == ReachStrategy.SOFT) {
            final Vector2 currentDirection = BaseLogic.normalize(x(), y(), waypoint.targetX(), waypoint.getTargetY());
            waypointDirection = Obj.nonNull(waypointDirection, currentDirection::cpy);

            if (waypointDirection.dot(currentDirection) < 0) {
                reach(waypoint);
            }
        } else if (reachStrategy == ReachStrategy.STRICT) {
            if (MathUtils.isEqual(x(), waypoint.targetX(), reachTolerance) && MathUtils.isEqual(y(), waypoint.getTargetY(), reachTolerance)) {
                move(waypoint.targetX(), waypoint.getTargetY());
                reach(waypoint);
            }
        }
    }

    private void reach(final Waypoint waypoint) {
        final Event event = waypoint.getEvent();
        if (event != null) {
            event.eventHandling();
        }

        waypointIndex = ++waypointIndex % waypoints.size();
        waypointDirection = null;
        chillFrames = waypoint.freezeFrames();
    }
}
