package pojahn.game.desktop.redguyruns.levels.phanto;

import com.badlogic.gdx.audio.Sound;
import pojahn.game.core.MobileEntity;
import pojahn.game.essentials.Vibrator;

public class BigRoller extends MobileEntity {

    private float accX, maxSpeed, vel, rollSpeed;
    private boolean movingLeft;
    private int counter;
    private Vibrator rollingVib, crashingVib;
    private Sound rollSound, crashSound;

    public BigRoller(final float x, final float y) {
        move(x, y);
        accX = 200f;
        maxSpeed = 400f;
        rollSpeed = 100;
    }

    public void setRollingVib(final Vibrator rollingVib) {
        this.rollingVib = rollingVib;
    }

    public void setCrashingVib(final Vibrator crashingVib) {
        this.crashingVib = crashingVib;
    }

    public void setRollSound(final Sound rollSound) {
        this.rollSound = rollSound;
    }

    public void setCrashSound(final Sound crashSound) {
        this.crashSound = crashSound;
    }

    public void setRollSpeed(final float rollSpeed) {
        this.rollSpeed = rollSpeed;
    }

    @Override
    public void logistics() {
        if (isFrozen())
            return;

        if (movingLeft) {
            if (vel < maxSpeed)
                vel += accX * getEngine().delta;

            final float futureX = getFutureX();
            if (occupiedAt(futureX, y())) {
                movingLeft = false;
                reached();
            } else {
                move(futureX, y());
            }
        } else {
            if (-vel < maxSpeed)
                vel -= accX * getEngine().delta;

            final float futureX = getFutureX();
            if (occupiedAt(futureX, y())) {
                movingLeft = true;
                reached();
            } else {
                move(futureX, y());
            }
        }

        if (++counter % 10 == 0) {
            rollingVib.vibrate();
            sounds.play(rollSound);
        }

        bounds.rotation += -(vel / rollSpeed);
    }

    private void reached() {
        freeze();
        vel = 0;
        crashingVib.vibrate();
        getLevel().runOnceAfter(this::unfreeze, 120);
        sounds.play(crashSound);
    }

    private float getFutureX() {
        return bounds.pos.x - vel * getEngine().delta;
    }
}
