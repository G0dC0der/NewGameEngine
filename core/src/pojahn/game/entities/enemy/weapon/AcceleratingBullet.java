package pojahn.game.entities.enemy.weapon;

import com.badlogic.gdx.math.Vector2;
import pojahn.game.core.Entity;

public class AcceleratingBullet extends Projectile {

    private float acceleration;
    private float speed;

    public AcceleratingBullet(final Entity... scanTargets) {
        super(0, 0, scanTargets);
        follow(false);
        acceleration = .08f;
    }

    @Override
    public AcceleratingBullet getClone() {
        final AcceleratingBullet clone = new AcceleratingBullet(getSubjects().toArray(new Entity[0]));
        copyData(clone);
        if (cloneEvent != null)
            cloneEvent.handleClonded(clone);

        return clone;
    }

    public void setAcceleration(final float acceleration) {
        this.acceleration = acceleration;
    }

    public void setSpeed(final float speed) {
        this.speed = speed;
    }

    protected void copyData(final AcceleratingBullet clone) {
        super.copyData(clone);
        clone.acceleration = acceleration;
    }

    @Override
    protected void moveProjectile(final Vector2 target) {
        speed = Math.min(speed + acceleration, getMoveSpeed());
        dumbMoveTowards(target.x, target.y, speed);
    }
}