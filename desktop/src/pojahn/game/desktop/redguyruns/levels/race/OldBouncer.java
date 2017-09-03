package pojahn.game.desktop.redguyruns.levels.race;

import com.badlogic.gdx.audio.Sound;
import pojahn.game.core.Collisions;
import pojahn.game.core.MobileEntity;
import pojahn.game.entities.PathDrone;
import pojahn.game.entities.mains.GravityMan;
import pojahn.game.essentials.Direction;

import java.util.Random;

class OldBouncer extends PathDrone {

    private float bounceStrength, shakeX, shakeY;
    private int times, shakeCounter, shakeTime;
    private final GravityMan[] victims;
    private int[] pushCounter;
    private Direction explicitDir, victimDirrections[];
    private boolean shake;
    private final Random r;
    private Sound shakeSound;
    private int shakeSoundDelay, shakeSoundCounter;

    OldBouncer(float x, float y, float bounceStrength, int times, Direction explicitDir, GravityMan... victims) {
        super(x,y);
        this.times = times;
        this.bounceStrength = bounceStrength;
        this.explicitDir = explicitDir;
        this.victims = victims;
        victimDirrections = new Direction[victims.length];
        pushCounter = new int[victims.length];
        shakeCounter = 0;
        r = new Random();
    }

    @Override
    public OldBouncer getClone() {
        OldBouncer b = new OldBouncer(x(), y(), bounceStrength, times, explicitDir, victims);
        copyData(b);

        if (cloneEvent != null)
            cloneEvent.handleClonded(b);

        return b;
    }

    protected void copyData(OldBouncer clone) {
        super.copyData(clone);

        clone.shakeX = shakeX;
        clone.shakeY = shakeY;
        clone.shakeTime = shakeTime;
        clone.shake = shake;
    }

    void setShake(boolean shake, int shakeTime, float shakeX, float shakeY) {
        this.shake = shake;
        this.shakeTime = shakeTime;
        this.shakeX = shakeX;
        this.shakeY = shakeY;
    }

    void setShakeSound(Sound sound, int delay) {
        shakeSound = sound;
        shakeSoundDelay = delay;
    }

    @Override
    public void logistics() {
        super.logistics();
        ++shakeSoundCounter;

        if (shake && shakeCounter-- > 0) {
            float x = (float) r.nextGaussian(),
                    y = (float) r.nextGaussian();

            offsetX = x + ((x > 0.5f) ? shakeX : -shakeX);
            offsetY = y + ((y > 0.5f) ? shakeY : -shakeY);
        } else
            offsetX = offsetY = 0;
        float middleX = centerX(),
                middleY = centerY();

        for (int i = 0; i < victims.length; i++) {
            MobileEntity mo = victims[i];

            if (!mo.isActive())
                continue;

            if (!collidesWith(mo))
                victimDirrections[i] = Collisions.getDirection(Collisions.normalize(middleX, middleY, mo.prevX() + mo.halfWidth(), mo.prevY() + mo.halfHeight()));
            else {
                pushCounter[i] = times;
                if (shakeSound != null && shakeSoundCounter > shakeSoundDelay) {
                    shakeSoundCounter = 0;
                    shakeSound.play(sounds.calc());
                }
            }
        }
        pushSubjects();
    }

    private void pushSubjects() {
        for (int i = 0; i < victims.length; i++) {
            if (pushCounter[i]-- <= 0)
                continue;

            shakeCounter = shakeTime;

            if (pushCounter[i]-- == times) {
            }

            GravityMan man = victims[i];

            Direction pushingDirr = (explicitDir == null) ? victimDirrections[i] : explicitDir;
            switch (pushingDirr) {
                case N:
                    man.vel.y = bounceStrength;
                    break;
                case NE:
                    man.vel.y = bounceStrength;
                    man.vel.x = -bounceStrength;
                    break;
                case E:
                    man.vel.x = -bounceStrength;
                    break;
                case SE:
                    man.vel.y = -bounceStrength;
                    man.vel.x = -bounceStrength;
                    break;
                case S:
                    man.vel.y = -bounceStrength;
                    break;
                case SW:
                    man.vel.y = -bounceStrength;
                    man.vel.x = bounceStrength;
                    break;
                case W:
                    man.vel.x = bounceStrength;
                    break;
                case NW:
                    man.vel.y = bounceStrength;
                    man.vel.x = bounceStrength;
                    break;
            }
        }
    }
}
