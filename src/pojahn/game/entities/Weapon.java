package pojahn.game.entities;

import com.badlogic.gdx.math.Vector2;
import pojahn.game.core.Collisions;
import pojahn.game.core.Entity;
import pojahn.game.core.Level;
import pojahn.game.essentials.Animation;
import pojahn.game.essentials.Image2D;

public class Weapon extends PathDrone {

    private float targetX, targetY, firingOffsetX, firingOffsetY, rotationSpeed;
    private int burst, burstDelay, reload, burstCounter, delayCounter, reloadCounter;
    private boolean rotationAllowed, alwaysRotate, frontFire, firing, rotateWhileRecover;
    private Entity targets[], currTarget, dummy;
    private Projectile proj;
    private Particle firingParticle;
    private Animation<Image2D> firingImage, orgImage;
    private boolean usingTemp;

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
        dummy = new Entity();
    }

    @Override
    public void logistics() {
        if (proj == null)
            throw new IllegalStateException("The projectile must be set before usage.");

        if (usingTemp && getImage().hasEnded()) {
            usingTemp = false;
            firingImage = getImage();
            firingImage.reset();
            setImage(orgImage);
        }

        super.logistics();

        findTarget();

        rotateWeapon();

        if (--reloadCounter > 0)
            return;
        else if (reloadCounter == 0)
            rotationAllowed = true;

        if (haveTarget()) {
            if (!haveBullets())
                reset();
            else if ((firing || isTargeting()) && ++delayCounter % burstDelay == 0) {
                if (firingImage != null) {
                    orgImage = getImage();
                    setImage(firingImage);
                    usingTemp = true;
                }
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
                projClone.setTarget(targetX, targetY);
                getLevel().add(projClone);
                if (partClone != null)
                    getLevel().add(partClone);
            }
        } else
            reset();
    }

    public boolean haveTarget() {
        return currTarget != null;
    }

    public boolean haveBullets() {
        return burst > burstCounter;
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

    public void setFiringImage(Animation<Image2D> firingImage) {
        this.firingImage = firingImage;
    }

    public void setRotateWhileRecover(boolean rotateWhileRecover) {
        this.rotateWhileRecover = rotateWhileRecover;
    }

    protected void findTarget() {
        currTarget = Collisions.findClosestSeeable(this, targets);
    }

    protected void rotateWeapon() {
        if (rotationAllowed && rotationSpeed != 0.0f && haveTarget()) {
            float x1 = centerX(),
                    y1 = centerY(),
                    x2 = currTarget.centerX(),
                    y2 = currTarget.centerY();

            if (alwaysRotate || currTarget.canSee(this))
                bounds.rotation = Collisions.rotateTowardsPoint(x1, y1, x2, y2, bounds.rotation, rotationSpeed);
        }
    }

    protected void reset() {
        burstCounter = 0;
        delayCounter = burstDelay - 1;
        reloadCounter = reload;
        rotationAllowed = rotateWhileRecover;
        firing = false;
        currTarget = null;
    }

    protected boolean isTargeting() {
        if (rotationSpeed == 0.0f) {
            Vector2 position = Collisions.findEdgePoint(this, currTarget, getLevel());
            targetX = position.x;
            targetY = position.y;
            return canSee(currTarget);
        }

        float centerX = centerX();
        float centerY = centerY();

        Vector2 front = getFrontPosition();
        Vector2 edge = Collisions.findEdgePoint(centerX, centerY, front.x, front.y, getLevel());
        Vector2 wall = Collisions.searchTile((int) centerX, (int) centerY, (int) edge.x, (int) edge.y, false, Level.Tile.SOLID, getLevel());

        dummy.bounds.pos.x = currTarget.bounds.pos.x + currTarget.halfWidth();
        dummy.bounds.pos.y = currTarget.bounds.pos.y + currTarget.halfHeight();
        boolean targeting = Collisions.lineRectangle((int) centerX, (int) centerY, (int) wall.x, (int) wall.y, dummy.bounds.toRectangle());
        if (targeting) {
            Vector2 edge2 = Collisions.findEdgePoint(this, dummy, getLevel());
            targetX = edge2.x;
            targetY = edge2.y;
        }
        return targeting;
    }
}