package pojahn.game.entities;

import com.badlogic.gdx.math.Vector2;
import pojahn.game.core.BaseLogic;
import pojahn.game.core.Entity;
import pojahn.game.core.Level;
import pojahn.game.core.MobileEntity;
import pojahn.game.essentials.EntityBuilder;
import pojahn.lang.Obj;

import java.util.List;

import static java.util.stream.Stream.concat;
import static java.util.stream.Stream.of;

/**
 * You can see 'targets' as the objects the projectile can interact with and 'target' the object the projectile is firing at.
 */
public abstract class Projectile extends MobileEntity {

    private Particle impact, gunfire, trailer;
    private final List<Entity> targets;
    private Entity target;
    private Vector2 cachedTarget;
    private boolean rotate;
    private int trailerDelay;
    private int trailerCounter;
    private boolean follow, once;

    public Projectile(final float x, final float y, final Entity... targets) {
        move(x, y);
        rotate = true;
        this.targets = Obj.requireNotEmpty(targets);
        trailerDelay = 3;
    }

    @Override
    public Projectile getClone() {
        throw new NullPointerException("Can not get a clone of an abstract class.");
    }

    public void setImpact(final Particle impact) {
        this.impact = impact;
    }

    public void setGunfire(final Particle gunfire) {
        this.gunfire = gunfire;
    }

    public void setTrailer(final Particle trailer) {
        this.trailer = trailer;
    }

    public void setTrailerDelay(final int trailerDelay) {
        this.trailerDelay = trailerDelay;
    }

    public void rotate(final boolean rotate) {
        this.rotate = rotate;
    }

    public boolean rotates() {
        return rotate;
    }

    public void setTarget(final Vector2 target) {
        setTarget(target.x, target.y);
    }

    public void setTarget(final float x, final float y) {
        setTarget(new EntityBuilder().move(x, y).build());
    }

    public void setTarget(final Entity target) {
        this.target = target;
    }

    public void follow(final boolean lockTarget) {
        this.follow = lockTarget;
    }

    public List<Entity> getTargets() {
        return targets;
    }

    @Override
    public final void logistics() {
        if (target != null) {

            moveProjectile(getTarget());
            collisionCheck();

            if (rotate)
                rotate();

            if (trailer != null && ++trailerCounter % trailerDelay == 0) {
                final Vector2 rare = getRarePosition();
                getLevel().add(trailer.getClone().move(rare.x - trailer.halfWidth(), rare.y - trailer.halfHeight()));
            }

            if (gunfire != null && !once) {
                once = true;
                getLevel().add(gunfire.getClone().center(this));
            }
        }
    }

    protected abstract void moveProjectile(Vector2 target);

    protected void rotate() {
        final Vector2 target = getTarget();
        bounds.rotation = (float) BaseLogic.getAngle(centerX(), centerY(), target.x, target.y);
    }

    private Vector2 getTarget() {
        if (follow) {
            return target.getPos();
        } else {
            if (cachedTarget == null)
                cachedTarget = BaseLogic.findEdgePoint(x(), y(), target.x(), target.y(), getLevel());

            return cachedTarget;
        }
    }

    private void collisionCheck() {
        if (outOfBounds() || getOccupyingCells().contains(Level.Tile.SOLID)) {
            impact(null);
        } else {
            concat(targets.stream(), of(target))
                .distinct()
                .filter(Entity::isActive)
                .filter(this::collidesWith)
                .findFirst()
                .ifPresent(this::impact);
        }
    }

    private void impact(final Entity victim) {
        if (victim != null && victim.hasActionEvent())
            victim.runActionEvent(this);

        final Vector2 front = getFrontPosition();
        final Level l = getLevel();

        if (impact != null)
            l.add(impact.getClone().move(front.x - impact.halfWidth(), front.y - impact.halfHeight()));

        l.discard(this);
    }

    protected void copyData(final Projectile clone) {
        super.copyData(clone);
        clone.impact = impact;
        clone.gunfire = gunfire;
        clone.trailer = trailer;
        clone.trailerDelay = trailerDelay;
        clone.rotate = rotate;
        clone.follow = follow;
    }
}