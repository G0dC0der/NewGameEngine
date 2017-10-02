package pojahn.game.entities;

import com.badlogic.gdx.math.Vector2;
import pojahn.game.core.Collisions;
import pojahn.game.core.Entity;

import java.util.stream.Stream;

public class Weapon extends PathDrone {

    private float firingOffsetX, firingOffsetY, rotationSpeed;
    private int burst, burstDelay, reload, burstCounter, delayCounter, reloadCounter;
    private boolean rotationAllowed, alwaysRotate, frontFire, firing, rotateWhileRecover, targeting, ignoreInactive;
    private Entity targets[], currTarget;
    private Projectile proj;
    private Particle firingParticle;

    public Weapon(final float x, final float y, final int burst, final int burstDelay, final int emptyReload, final Entity... targets) {
        super(x, y);
        this.burst = burst;
        this.reload = emptyReload;
        this.targets = targets;
        this.burstDelay = burstDelay;
        alwaysRotate = firing = false;
        rotationAllowed = rotateWhileRecover = true;
        burstCounter = reloadCounter = 0;
        delayCounter = burstDelay - 1;
        ignoreInactive = true;
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

        currTarget = Collisions.findClosestSeeable(this, getTargets());
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

                final Projectile projClone;
                Particle partClone = null;
                if (frontFire) {
                    final Vector2 front = getFrontPosition();
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

    public void setProjectile(final Projectile proj) {
        this.proj = proj;
    }

    public void setFiringOffsets(final float x, final float y) {
        firingOffsetX = x;
        firingOffsetY = y;
    }

    public void setFiringParticle(final Particle firingParticle) {
        this.firingParticle = firingParticle;
    }

    public void setFrontFire(final boolean frontFire) {
        this.frontFire = frontFire;
    }

    public void setRotationSpeed(final float rotationSpeed) {
        this.rotationSpeed = rotationSpeed;
    }

    public void setAlwaysRotate(final boolean alwaysRotate) {
        this.alwaysRotate = alwaysRotate;
    }

    public void setRotateWhileRecover(final boolean rotateWhileRecover) {
        this.rotateWhileRecover = rotateWhileRecover;
    }

    public void setIgnoreInactive(final boolean ignoreInactive) {
        this.ignoreInactive = ignoreInactive;
    }

    private void rotateWeapon() {
        Entity target = null;

        if (currTarget == null && alwaysRotate) {
            target = Collisions.findClosest(this, getTargets());
        } else if (rotationAllowed && rotationSpeed != 0.0f && targets != null && canSee(currTarget)) {
            target = currTarget;
        }

        if (target != null)
            bounds.rotation = Collisions.rotateTowardsPoint(centerX(), centerY(), target.centerX(), target.centerY(), bounds.rotation, rotationSpeed);
    }

    private Entity[] getTargets() {
        return Stream.of(targets)
                .filter(entity -> !ignoreInactive || entity.isActive())
                .toArray(Entity[]::new);
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
                final Vector2 front = getFrontPosition();
                final Vector2 edge = Collisions.findEdgePoint(centerX(), centerY(), front.x, front.y, getLevel());

                return Collisions.lineRectangle(front.x, front.y, edge.x, edge.y, currTarget.bounds.toRectangle()) && canSee(currTarget);
            }
        } else
            return false;
    }
}