package pojahn.game.desktop.redguyruns.levels.orbit;

import pojahn.game.core.Collisions;
import pojahn.game.core.Entity;

class Fireball extends Entity {

    private float initialX, initialY, mass, gravity, damping, vy, flyPower;

    Fireball(float initialX, float initialY, float flyPower) {
        move(initialX, initialY);
        this.initialX = initialX;
        this.initialY = initialY;
        this.flyPower = flyPower;

        mass = 1.0f;
        gravity = -500;
        damping = 0.0001f;
    }

    void setFlyPower(float flyPower) {
        this.flyPower = flyPower;
    }

    @Override
    public void logistics() {
        if (Collisions.distance(initialX, initialY, x(), y()) < 30)
            vy = flyPower;

        vy *= 1.0 - (damping * getEngine().delta);
        float force = mass * gravity;
        vy += (force / mass) * getEngine().delta;

        bounds.pos.y -= vy * getEngine().delta;
    }
}
