package pojahn.game.essentials;

import com.badlogic.gdx.math.Vector2;
import pojahn.game.core.MobileEntity;

public class GravityAware {

    private final MobileEntity source;
    public final Vector2 velocity;
    public float accX, mass, gravity, damping, maxX, maxY, delta;

    public GravityAware(final MobileEntity source) {
        this.source = source;
        velocity = new Vector2();
        maxX = 260;
        maxY = -800;
        accX = 650;
        mass = 1.0f;
        gravity = -500;
        damping = 0.0001f;
        delta = 1.0f/60.0f;
    }

    public void glide() {
        if (velocity.x > 0) {
            moveRight();
            if (velocity.x < 0)
                velocity.x = 0;
        } else if (velocity.x < 0) {
            moveLeft();
            if (velocity.x > 0)
                velocity.x = 0;
        }
    }

    public void land() {
        if (velocity.y > 0) {
            drag();
            if (velocity.x < 0)
                velocity.y = 0;
        } else if (velocity.y < 0) {
            pull();
            if (velocity.y > 0)
                velocity.y = 0;
        }
    }

    public void moveLeft() {
        if (velocity.x < maxX)
            velocity.x += accX * delta;

        moveToX();
    }

    public void moveRight() {
        if (-velocity.x < maxX)
            velocity.x -= accX * delta;

        moveToX();
    }

    public void pull() { //TODO: Test
        final float force = mass * gravity;
        velocity.y *= 1.0 - (damping * delta);

        if (maxY < -velocity.y) {
            velocity.y -= (force / mass) * delta;
        } else
            velocity.y += (force / mass) * delta;

        moveToY();
    }

    public void drag() {
        final float force = mass * gravity;
        velocity.y *= 1.0 - (damping * delta);

        if (maxY < velocity.y) {
            velocity.y += (force / mass) * delta;
        } else
            velocity.y -= (force / mass) * delta;

        moveToY();
    }

    private void moveToX() {
        if (velocity.x != 0) {
            final float futureX = source.bounds.pos.x - velocity.x * delta;

            if (!source.occupiedAt(futureX, source.y())) {
                source.move(futureX, source.y());
            } else {
                velocity.x = 0;
            }
        }
    }

    private void moveToY() {
        if (velocity.y != 0) {
            float futureY = source.bounds.pos.y - velocity.y * delta;

            if (!source.occupiedAt(source.x(), futureY)) {
                source.move(source.x(), futureY);
            } else {
                velocity.y = 0;
            }
        }
    }
}