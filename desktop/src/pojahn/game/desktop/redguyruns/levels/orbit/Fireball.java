package pojahn.game.desktop.redguyruns.levels.orbit;

import pojahn.game.core.BaseLogic;
import pojahn.game.core.Entity;

public class Fireball extends Entity {

    private float initialX, initialY, mass, gravity, damping, vy, flyPower;

    public Fireball(final float initialX, final float initialY, final float flyPower) {
        move(initialX, initialY);
        this.initialX = initialX;
        this.initialY = initialY;
        this.flyPower = flyPower;

        mass = 1.0f;
        gravity = -500;
        damping = 0.0001f;
    }

    void setFlyPower(final float flyPower) {
        this.flyPower = flyPower;
    }

    @Override
    public void logistics() {
        if (BaseLogic.distance(initialX, initialY, x(), y()) < 30)
            vy = flyPower;

        vy *= 1.0 - (damping * getEngine().delta);
        final float force = mass * gravity;
        vy += (force / mass) * getEngine().delta;

        bounds.pos.y -= vy * getEngine().delta;
    }
}
