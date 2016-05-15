package pojahn.game.entities;

import com.badlogic.gdx.math.Vector2;
import pojahn.game.core.Collisions;
import pojahn.game.core.Entity;
import pojahn.game.core.Level;
import pojahn.game.core.Level.Tile;
import pojahn.game.core.MobileEntity;
import pojahn.game.essentials.EntityBuilder;
import static java.util.stream.Stream.concat;
import static java.util.stream.Stream.of;

public abstract class Projectile extends MobileEntity {

    private Particle impact, gunfire, trailer;
    private Entity targets[], target;
    private Vector2 lockedTarget;
    private boolean rotate;
    private int trailerDelay;
    private int trailerCounter;
    private boolean fired, lock;
    private float initialX, initialY;

    public Projectile(float x, float y, Entity... targets) {
        move(x, y);
        initialX = x;
        initialY = y;
        rotate = true;
        this.targets = targets;
        trailerDelay = 3;
    }

    @Override
    public Projectile getClone() {
        throw new NullPointerException("Can not get a clone of an abstract class.");
    }

    public void setImpact(Particle impact) {
        this.impact = impact;
    }

    public void setGunfire(Particle gunfire) {
        this.gunfire = gunfire;
    }

    public void setTrailer(Particle trailer) {
        this.trailer = trailer;
    }

    public void setTrailerDelay(int trailerDelay) {
        this.trailerDelay = trailerDelay;
    }

    public void rotate(boolean rotate) {
        this.rotate = rotate;
    }

    public boolean rotates() {
        return rotate;
    }

    public void setTarget(Vector2 target) {
        this.target = new EntityBuilder().move(target).build();
        lockedTarget = new Vector2(target);
    }

    public void setTarget(float x, float y) {
        this.target = new EntityBuilder().move(x, y).build();
        lockedTarget = new Vector2(x,y);
    }

    public void setTarget(Entity target) {
        this.target = target;
        lockedTarget = target.getPos();
    }

    public void lock(boolean lockTarget) {
        this.lock = lockTarget;
    }

    @Override
    public final void logistics() {
        if(target == null) {
            target = Collisions.findClosestSeeable(this, targets);
            if (target != null) {
                lockedTarget = target.getPos();
                fire();
            }
        } else if(!fired){
            fire();
        }

        if (fired) {
            moveProjectile(getTarget());
            collisionCheck();
            if(rotate)
                rotate();

            if (trailer != null && ++trailerCounter % trailerDelay == 0) {
                Vector2 rare = getRarePosition();
                getLevel().add(trailer.getClone().move(rare.x - trailer.halfWidth(), rare.y - trailer.halfHeight()));
            }
        }
    }

    protected abstract void moveProjectile(Vector2 target);

    protected void rotate() {
        Vector2 target = getTarget();
        bounds.rotation = (float) Collisions.getAngle(centerX(), centerY(), target.x, target.y);
    }

    protected Vector2 getTarget() {
        Vector2 targetPos = lock ? lockedTarget : target.getPos();
        float x = lock ? initialX : bounds.pos.x;
        float y = lock ? initialY : bounds.pos.y;
        return Collisions.findEdgePoint(x, y, targetPos.x, targetPos.y, getLevel());
    }

    private void fire() {
        fired = true;
        if (gunfire != null)
            getLevel().add(gunfire.getClone().center(this));
    }

    private void collisionCheck() {
        Level l = getLevel();

        if (l.tileAt(getFrontPosition()) == Tile.SOLID || l.tileAt(getRarePosition()) == Tile.SOLID) {
            impact(null);
        } else {
            concat(of(targets), of(target))
                    .filter(this::collidesWith)
                    .findFirst()
                    .ifPresent(this::impact);
        }
    }

    private void impact(Entity victim) {
        if (victim != null && victim.hasActionEvent())
            victim.runActionEvent(this);

        Vector2 front = getFrontPosition();
        Level l = getLevel();

        if (impact != null)
            l.add(impact.getClone().move(front.x - impact.halfWidth(), front.y - impact.halfHeight()));

        l.discard(this);
    }

    protected void copyData(Projectile clone) {
        super.copyData(clone);
        clone.impact = impact;
        clone.gunfire = gunfire;
        clone.trailer = trailer;
        clone.trailerDelay = trailerDelay;
        clone.rotate = rotate;
        clone.targets = targets;
        clone.lock = lock;
    }
}