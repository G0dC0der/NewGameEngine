package pojahn.game.entities;

import com.badlogic.gdx.math.Vector2;
import pojahn.game.core.Entity;

public class Bullet extends Projectile {

    public Bullet(Entity... scanTargets) {
        this(0,0,scanTargets);
    }

    public Bullet(float x, float y, Entity... scanTargets) {
        super(x, y, scanTargets);
        follow(false);
    }

    @Override
    public Bullet getClone() {
        Bullet clone = new Bullet(x(), y(), getTargets());
        copyData(clone);
        if (cloneEvent != null)
            cloneEvent.handleClonded(clone);

        return clone;
    }

    @Override
    protected void moveProjectile(Vector2 target) {
        moveTowards(target.x, target.y);
    }
}