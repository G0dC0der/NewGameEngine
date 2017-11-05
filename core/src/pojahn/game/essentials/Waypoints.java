package pojahn.game.essentials;

import pojahn.game.entities.movement.PathDrone;

public class Waypoints {

    public static void rectangularMovement(final PathDrone drone, final float width, final float height) {
        drone.appendPath();
        drone.appendPath(drone.x() + width, drone.y());
        drone.appendPath(drone.x() + width, drone.y() + drone.height());
        drone.appendPath(drone.x(), drone.y() + drone.height());
    }
}
