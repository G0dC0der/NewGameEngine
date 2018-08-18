package pojahn.game.desktop.redguyruns.levels.bcastle;

import pojahn.game.core.Entity;
import pojahn.game.core.MobileEntity;
import pojahn.game.entities.particle.Particle;
import pojahn.game.essentials.Vibrator;

import java.util.List;

public class RollingThwump extends MobileEntity {

    private float fallingLine,  mass, gravity, damping, vy, leftSpeed, rotationSpeed;
    private boolean moveLeft;
    private Vibrator landingVib, dieVib;
    private List<Entity> dieEntities;
    private Particle dieParticle;

    public RollingThwump() {
        mass = 1.0f;
        gravity = -500;
        damping = 0.0001f;

        leftSpeed = .2f;
    }

    public void setFallingLine(final float fallingLine) {
        this.fallingLine = fallingLine;
    }

    public void setLandingVib(final Vibrator landingVib) {
        this.landingVib = landingVib;
    }

    public void setDieVib(final Vibrator dieVib) {
        this.dieVib = dieVib;
    }

    public void setDieEntities(final List<Entity> dieEntities) {
        this.dieEntities = dieEntities;
    }

    public void setDieParticle(final Particle dieParticle) {
        this.dieParticle = dieParticle;
    }

    @Override
    public void logistics() {
        if (isFrozen())
            return;

        if (!moveLeft) {
            if (fallingLine > y()) {
                moveTowards(x(), y() + 10000);
            } else {
                vy *= 1.0 - (damping * getEngine().delta);
                final float force = mass * gravity;
                vy += (force / mass) * getEngine().delta;

                final float nextY = bounds.pos.y - vy * getEngine().delta;
                if (!occupiedAt(x(), nextY)) {
                    move(x(), nextY);
                } else {
                    moveLeft = true;
                    tryDown(5);
                    landingVib.vibrate();

                    getLevel().runOnceAfter(this::unfreeze, 20);
                    freeze();
                }
            }
        } else {
            dumbMoveTowards(0, y(), leftSpeed);
            leftSpeed = Math.min(leftSpeed + .03f, 3);

            rotate(rotationSpeed);
            rotationSpeed = Math.max(rotationSpeed - .04f, -4.5f);

            if (dieEntities.stream().anyMatch(this::collidesWith)) {
                //TODO: Destroy sound
                die();
                dieVib.vibrate();
                getLevel().add(dieParticle.getClone().center(this));
            }
        }
    }
}
