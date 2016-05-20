package pojahn.game.entities;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import pojahn.game.core.MobileEntity;
import pojahn.game.essentials.Hitbox;

import static pojahn.game.core.Collisions.rectanglesCollide;

public class PushableObject extends MobileEntity {

    public float mass, gravity, damping, fallSpeedLimit, deacceleration, pushStrength;
    private Vector2 vel;
    private boolean useGravity, mustStand;
    private MobileEntity[] pushers;
    private Rectangle dummy;
    private int pushingSoundDelay, pushingSoundCounter;
    private Sound landingSound, pushingSound;

    public PushableObject(float x, float y, MobileEntity... pushers) {
        vel = new Vector2();
        move(x, y);
        this.pushers = pushers;
        dummy = new Rectangle();
        for (MobileEntity mobile : pushers) {
            mobile.addObstacle(this);
            addObstacle(mobile);
        }

        useGravity = true;
        mass = 1.0f;
        gravity = -500;
        damping = 0.0001f;
        fallSpeedLimit = -1200;

        pushStrength = 300;
        deacceleration = 30;
    }

    @Override
    public void logistics() {
        final float DELTA = getEngine().delta;

        if (useGravity) {
            if (!canDown()) {
                if (vel.y < 0 && landingSound != null)
                    landingSound.play(sounds.calc());
                vel.y = 0;
            } else {
                drag();
                float nextY = bounds.pos.y - vel.y * DELTA;

                if (!occupiedAt(x(), nextY))
                    move(x(), nextY);
                else
                    tryDown(10);
            }
        }

        if (pushStrength > 0.0f) {
            dummy.set(x() - 2, y(), width() + 4, height());

            for (MobileEntity mobile : pushers) {
                if (!mustStand || !mobile.canDown()) {

                    if (rectanglesCollide(mobile.bounds.toRectangle(), dummy)) {
                        if (centerX() > mobile.centerX())
                            vel.x = -pushStrength;
                        else
                            vel.x = pushStrength;
                    }
                }
            }
        }

        if (vel.x != 0) {
            float nextX = bounds.pos.x - vel.x * getEngine().delta;
            if (!occupiedAt(nextX, y())) {
                move(nextX, y());

                if (vel.x > 0) {
                    vel.x -= deacceleration * DELTA;
                    if (vel.x < 0)
                        vel.x = 0;
                } else if (vel.x < 0) {
                    vel.x += deacceleration * DELTA;
                    if (vel.x > 0)
                        vel.x = 0;
                }
            } else {
                vel.x = 0;
            }
        }

        if (isMoving() && pushingSound != null && ++pushingSoundCounter % pushingSoundDelay == 0)
            pushingSound.play(sounds.calc());
    }

    public void useGravity(boolean gravity) {
        useGravity = gravity;
    }

    public void mustStand(boolean mustStand) {
        this.mustStand = mustStand;
    }

    public void setSlammingSound(Sound sound) {
        landingSound = sound;
    }

    public void setPushingSound(Sound sound, int delay) {
        pushingSound = sound;
        pushingSoundDelay = delay;
    }

    @Deprecated
    @Override
    public void setHitbox(Hitbox hitbox) {
        throw new UnsupportedOperationException("PushableObject is limited to rectangular hitbox.");
    }

    @Override
    public void dispose() {
        for (MobileEntity mobile : pushers)
            mobile.removeObstacle(this);
    }

    private void drag() {
        float force = mass * gravity;
        vel.y *= 1.0 - (damping * getEngine().delta);

        if (fallSpeedLimit < vel.y) {
            vel.y += (force / mass) * getEngine().delta;
        } else
            vel.y -= (force / mass) * getEngine().delta;
    }
}