package pojahn.game.entities;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import pojahn.game.core.Collisions;
import pojahn.game.core.Entity;
import pojahn.game.core.MobileEntity;
import pojahn.game.essentials.Hitbox;

import static pojahn.game.core.Collisions.*;

public class PushableObject extends MobileEntity {

    public float mass, gravity, damping, fallSpeedLimit, acceleration;
    private Vector2 vel;
    private float currAcc;

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

        acceleration = 300;
    }

    @Override
    public void logistics() {
        if (useGravity) {
            if (!canDown()) {
                if (vel.y < 0 && landingSound != null)
                    landingSound.play(sounds.calc());
                vel.y = 0;
            } else {
                drag();
                float nextY = bounds.pos.y - vel.y * getEngine().delta;

                if (!occupiedAt(x(), nextY))
                    move(x(), nextY);
                else
                    tryDown(10);
            }
        }

        if(acceleration > 0.0f) {
            for(MobileEntity mobile : pushers) {
                if(mustStand && mobile.canDown())
                    continue;

                dummy.set(x() - 2, y(), width() + 4, height());
                if(rectanglesCollide(mobile.bounds.toRectangle(), dummy)) {
                    currAcc = leftMost(mobile, this) == mobile ? acceleration : -acceleration;
                    break;
                }
            }
        }

        final float DELTA = getEngine().delta;
        if(currAcc > 0.0f) {
            vel.x += currAcc * DELTA;
        }

//        vel.x += acceleration * getEngine().delta;
//        bounds.pos.x = vel.x * getEngine().delta;

//        boolean moved = false;
//        if (pushStrength > 0.0f) {
//            for (MobileEntity mobile : pushers) {
//                dummy.set(x() - 2, y(), width() + 4, height());
//
//                if (mustStand && mobile.canDown())
//                    continue;
//
//                if (rectanglesCollide(mobile.bounds.toRectangle(), dummy)) {
//                    if (Collisions.leftMost(mobile, this) == mobile) {
//                        moveRight();
//                        moved = true;
//                        float nextX = getFutureX();
//                        if (!occupiedAt(nextX, y()))
//                            move(nextX, y());
//                        else
//                            tryRight(10);
//                    } else if (Collisions.rightMost(mobile, this) == mobile) {
//                        moveLeft();
//                        moved = true;
//                        float nextX = getFutureX();
//                        if (!occupiedAt(nextX, y()))
//                            move(nextX, y());
//                        else
//                            tryLeft(10);
//                    }
//                }
//            }
//        }
//
//        if (!moved) {
//            if (runningRight()) {
//                moveLeft();
//                if (runningLeft())
//                    vel.x = 0;
//            } else if (runningLeft()) {
//                moveRight();
//                if (runningRight())
//                    vel.x = 0;
//            }
//
//            float nextX = getFutureX();
//            if (!occupiedAt(nextX, y()))
//                move(nextX, y());
//        }

        if (x() != prevX() && pushingSound != null && ++pushingSoundCounter % pushingSoundDelay == 0)
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

    protected void drag() {
        float force = mass * gravity;
        vel.y *= 1.0 - (damping * getEngine().delta);

        if (fallSpeedLimit < vel.y) {
            vel.y += (force / mass) * getEngine().delta;
        } else
            vel.y -= (force / mass) * getEngine().delta;
    }

    protected float getFutureX() {
        return bounds.pos.x - vel.x * getEngine().delta;
    }


    protected boolean runningLeft() {
        return vel.x > 0;
    }

    protected boolean runningRight() {
        return vel.x < 0;
    }
}