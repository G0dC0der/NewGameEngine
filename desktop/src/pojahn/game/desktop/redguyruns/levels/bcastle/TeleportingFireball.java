package pojahn.game.desktop.redguyruns.levels.bcastle;

import pojahn.game.core.MobileEntity;

public class TeleportingFireball extends MobileEntity {

    private float initialX, initialY, mass, gravity, damping, vy, flyPower, threshold, fadeSpeed;

    public TeleportingFireball(final float initialX, final float initialY, final float flyPower) {
        move(initialX, initialY);
        this.initialX = initialX;
        this.initialY = initialY;
        this.flyPower = flyPower;

        mass = 1.0f;
        gravity = -500;
        damping = 0.0001f;
    }

    public void setThreshold(final float threshold) {
        this.threshold = threshold;
    }

    public void setFadeSpeed(final float fadeSpeed) {
        this.fadeSpeed = fadeSpeed;
    }

    @Override
    public void logistics() {
        vy *= 1.0 - (damping * getEngine().delta);
        final float force = mass * gravity;
        vy += (force / mass) * getEngine().delta;

        bounds.pos.y -= vy * getEngine().delta;

        if (threshold > vy) {
            tint.a = Math.max(tint.a - fadeSpeed, 0);
            if (tint.a == 0) {
                move(initialX, initialY);
                vy = flyPower;
                tint.a = 1;
            }
        }
    }
}
