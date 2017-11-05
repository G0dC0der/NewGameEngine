package pojahn.game.entities.movement;

import com.badlogic.gdx.math.Vector2;
import com.google.common.collect.ImmutableList;
import pojahn.game.core.BaseLogic;
import pojahn.game.core.MobileEntity;
import pojahn.game.essentials.GravityAware;
import pojahn.lang.Obj;

import java.util.ArrayList;
import java.util.List;

public class PhysicsPathDrone extends MobileEntity {

    private final List<MobileEntity> subjects;
    private final List<Waypoint> waypoints;
    private final GravityAware gravityAware;
    private Vector2 waypointDirection;
    private int waypointIndex, chillFrames;

    public PhysicsPathDrone(final MobileEntity... subjects) {
        this.subjects = ImmutableList.copyOf(subjects);
        waypoints = new ArrayList<>();
        gravityAware = new GravityAware(this);
    }

    public GravityAware getGravityAware() {
        return gravityAware;
    }

    public void deaccelerate() {
        gravityAware.velocity.x = gravityAware.velocity.y = 0;
    }

    @Override
    public void logistics() {
        if (isFrozen()) {
            return;
        }

        if (--chillFrames > 0) {
            gravityAware.glide();
            gravityAware.land();
            return;
        }

        final Waypoint waypoint = waypoints.get(waypointIndex);
        if (waypoint.targetX > x()) {
            gravityAware.moveRight();
        } else {
            gravityAware.moveLeft();
        }
        if (waypoint.targetY > y()) {
            gravityAware.pull();
        } else {
            gravityAware.drag();
        }

        final Vector2 currentDirection = BaseLogic.normalize(bounds.pos.x, bounds.pos.y, waypoint.targetX, waypoint.targetY);
        waypointDirection = Obj.nonNull(waypointDirection, currentDirection::cpy);

        if (waypointDirection.dot(currentDirection) < 0) {
            waypointIndex = ++waypointIndex % subjects.size();
            waypointDirection = null;
            chillFrames = waypoint.frames;

            if (waypoint.event != null) {
                waypoint.event.eventHandling();
            }
        }
    }
}
