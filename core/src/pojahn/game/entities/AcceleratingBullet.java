package pojahn.game.entities;

import com.badlogic.gdx.math.Vector2;
import pojahn.game.core.Entity;

public class AcceleratingBullet extends Projectile {

    private float acceleration;
    private float speed;

    public AcceleratingBullet(Entity... scanTargets) {
        super(0, 0, scanTargets);
        follow(false);
        acceleration = .08f;
    }

    @Override
    public AcceleratingBullet getClone() {
        AcceleratingBullet clone = new AcceleratingBullet(getTargets());
        copyData(clone);
        if (cloneEvent != null)
            cloneEvent.handleClonded(clone);

        return clone;
    }

    public void setAcceleration(float acceleration) {
        this.acceleration = acceleration;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    protected void copyData(AcceleratingBullet clone) {
        super.copyData(clone);
        clone.acceleration = acceleration;
    }

    @Override
    protected void moveProjectile(Vector2 target) {
        speed = Math.min(speed + acceleration, getMoveSpeed());
        dumbMoveTowards(target.x, target.y, speed);
    }
}