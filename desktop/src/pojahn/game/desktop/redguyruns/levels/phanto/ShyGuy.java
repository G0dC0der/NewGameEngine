package pojahn.game.desktop.redguyruns.levels.phanto;

import com.badlogic.gdx.math.Vector2;
import pojahn.game.entities.LineMovement;

public class ShyGuy extends LineMovement {

    private Vector2 vel;
    public float mass, gravity, damping;

    public ShyGuy() {
        super(Movement.HORIZONTAL);
        vel = new Vector2();
        mass = 1.0f;
        gravity = -500;
        damping = 0.0001f;
    }

    @Override
    public void setMovement(final Movement movement) {
        throw new UnsupportedOperationException("Shy Guy is restricted to horizontal movement.");
    }

    @Override
    public void logistics() {
        super.logistics();

        if (canDown() || vel.y > 0)
            drag();

        final float futureY = getFutureY();
        if (!occupiedAt(x(), futureY))
            applyYForces();
        else {
            if (vel.y < 0) {
                tryDown(3);
            }
            vel.y = 0;
        }
    }

    void drag() {
        final float force = mass * gravity;
        final float delta = getEngine().delta;
        vel.y *= 1.0 - (damping * delta);

        if (-800 < vel.y) {
            vel.y += (force / mass) * delta;
        } else
            vel.y -= (force / mass) * delta;
    }

    float getFutureY() {
        return bounds.pos.y - vel.y * getEngine().delta;
    }

    void applyYForces() {
        bounds.pos.y -= vel.y * getEngine().delta;
    }
}
