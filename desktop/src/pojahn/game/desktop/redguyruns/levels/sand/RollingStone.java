package pojahn.game.desktop.redguyruns.levels.sand;

import com.badlogic.gdx.math.Vector2;
import pojahn.game.core.BaseLogic;
import pojahn.game.core.MobileEntity;

class RollingStone extends MobileEntity {

    float mass, gravity, damping, fallSpeedLimit, vy, maxX;
    float oldStyleSpeed = .4f;
    float rotationSpeed = 10;
    Vector2 wp1, wp2;
    private int moveCounter;

    RollingStone() {
        mass = 1.0f;
        gravity = -500;
        damping = 0.0001f;
        fallSpeedLimit = -1200;
    }

    @Override
    public void logistics() {
        final int step = moveCounter % 4;

        if (step == 0) {
            move(wp1);
            moveCounter++;
        } else if (step == 1) {
            if (!reached(wp2)) {
                moveTowards(wp2.x, wp2.y);
            } else {
                moveCounter++;
            }
        } else if (step == 2) {
            drag();
            final float nextY = bounds.pos.y - vy * getEngine().delta;

            if (!occupiedAt(x(), nextY)) {
                move(x(), nextY);
            } else {
                //TODO: Slam sounds
                tryDown(3);
                moveCounter++;
                vy = 0;
            }
        } else if (step == 3) {
            rotate(rotationSpeed);
            moveTowards(999999, y());
            if (x() > maxX) {
                bounds.pos.x = maxX;
                //TODO: Slam sounds
            }

            drag();
            final float nextY = bounds.pos.y - vy * getEngine().delta;

            if (!occupiedAt(x(), nextY)) {
                move(x(), nextY);
            } else {
                //TODO: Slam sounds
                tryDown(3);
                vy = 0;
            }

            if (y() > getLevel().getHeight()) {
                moveCounter++;
                vy = 0;
            }
        }
    }

    private void drag() {
        final float force = mass * gravity;
        vy *= 1.0 - (damping * getEngine().delta);

        if (fallSpeedLimit < vy) {
            vy += (force / mass) * getEngine().delta;
        } else
            vy -= (force / mass) * getEngine().delta;
    }

    private boolean reached(final Vector2 vec) {
        return oldStyleSpeed + 2 > BaseLogic.distance(vec.x, vec.y, x(), y());
    }
}
