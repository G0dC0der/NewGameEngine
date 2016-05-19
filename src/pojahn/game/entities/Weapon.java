package pojahn.game.entities;

import com.badlogic.gdx.math.Vector2;
import pojahn.game.core.Collisions;
import pojahn.game.core.Entity;
import pojahn.game.core.Level;
import pojahn.game.essentials.Animation;
import pojahn.game.essentials.Image2D;

public class Weapon extends PathDrone {

    private float firingOffsetX, firingOffsetY, rotationSpeed;
    private int burst, burstDelay, reload, burstCounter, delayCounter, reloadCounter;
    private boolean rotationAllowed, alwaysRotate, frontFire, firing, rotateWhileRecover, targeting;
    private Entity targets[], currTarget;
    private Projectile proj;
    private Particle firingParticle;

    public Weapon(float x, float y, int burst, int burstDelay, int emptyReload, Entity... targets) {
        super(x, y);
        this.burst = burst;
        this.reload = emptyReload;
        this.targets = targets;
        this.burstDelay = burstDelay;
        alwaysRotate = firing = false;
        rotationAllowed = rotateWhileRecover = true;
        burstCounter = reloadCounter = 0;
        delayCounter = burstDelay - 1;
    }

    @Override
    public void init() {
        super.init();
        if (proj == null)
            throw new IllegalStateException("The projectile must be set before usage.");
    }

    @Override
    public void logistics() {
        super.logistics();

        currTarget = Collisions.findClosestSeeable(this, targets);
        targeting = targeting();
        rotateWeapon();

        if (--reloadCounter > 0)
            return;
        else if (reloadCounter == 0)
            rotationAllowed = true;

        if (currTarget != null) {
            if (burst <= burstCounter)
                reset();
            else if ((firing || targeting) && ++delayCounter % burstDelay == 0) {
                rotationAllowed = false;
                firing = true;
                burstCounter++;

                Projectile projClone;
                Particle partClone = null;
                if (frontFire) {
                    Vector2 front = getFrontPosition();
                    projClone = proj.getClone();
                    projClone.move(front.x - proj.halfWidth() + firingOffsetX, front.y - proj.halfHeight() + firingOffsetY);
                    if (firingParticle != null) {
                        partClone = firingParticle.getClone();
                        partClone.move(front.x - firingParticle.halfWidth() + firingOffsetX, front.y - firingParticle.halfHeight() + firingOffsetY);
                    }
                } else {
                    projClone = proj.getClone();
                    projClone.move(bounds.pos.x + firingOffsetX, bounds.pos.y + firingOffsetY);
                    if (firingParticle != null) {
                        partClone = firingParticle.getClone();
                        partClone.move(bounds.pos.x + firingOffsetX, bounds.pos.y + firingOffsetY);

                    }
                }
                projClone.setTarget(currTarget);
                getLevel().add(projClone);
                if (partClone != null)
                    getLevel().add(partClone);
            }
        } else
            reset();
    }

    public boolean fireState() {
        return targeting;
    }

    public void setProjectile(Projectile proj) {
        this.proj = proj;
    }

    public void setFiringOffsets(float x, float y) {
        firingOffsetX = x;
        firingOffsetY = y;
    }

    public void setFiringParticle(Particle firingParticle) {
        this.firingParticle = firingParticle;
    }

    public void setFrontFire(boolean frontFire) {
        this.frontFire = frontFire;
    }

    public void setRotationSpeed(float rotationSpeed) {
        this.rotationSpeed = rotationSpeed;
    }

    public void setAlwaysRotate(boolean alwaysRotate) {
        this.alwaysRotate = alwaysRotate;
    }

    public void setRotateWhileRecover(boolean rotateWhileRecover) {
        this.rotateWhileRecover = rotateWhileRecover;
    }

    private void rotateWeapon() {
        Entity target = null;

        if(currTarget == null && alwaysRotate) {
            target = Collisions.findClosest(this, targets);
        } else if (rotationAllowed && rotationSpeed != 0.0f && targets != null && canSee(currTarget)) {
            target = currTarget;
        }

        if(target != null)
            bounds.rotation = Collisions.rotateTowardsPoint(centerX(), centerY(), target.centerX(), target.centerY(), bounds.rotation, rotationSpeed);
    }

    private void reset() {
        burstCounter = 0;
        delayCounter = burstDelay - 1;
        reloadCounter = reload;
        rotationAllowed = rotateWhileRecover;
        firing = false;
        currTarget = null;
    }

    private boolean targeting() {
        if (currTarget != null) {
            if (rotationSpeed == 0.0f) {
                return canSee(currTarget);
            } else {
                Vector2 front = getFrontPosition();
                Vector2 edge = Collisions.findEdgePoint(centerX(), centerY(), front.x, front.y, getLevel());

                return Collisions.lineRectangle(front.x, front.y, edge.x, edge.y, currTarget.bounds.toRectangle()) && canSee(currTarget);
            }
        } else
            return false;
    }
}