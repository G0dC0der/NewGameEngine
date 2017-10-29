package pojahn.game.entities.enemy.weapon;

import com.badlogic.gdx.math.Vector2;
import pojahn.game.core.Entity;

public class Bullet extends Projectile {

    public Bullet(final Entity... scanTargets) {
        this(0, 0, scanTargets);
    }

    public Bullet(final float x, final float y, final Entity... scanTargets) {
        super(x, y, scanTargets);
        follow(false);
    }

    @Override
    public Bullet getClone() {
        final Bullet clone = new Bullet(x(), y(), getSubjects().toArray(new Entity[0]));
        copyData(clone);
        if (cloneEvent != null)
            cloneEvent.handleClonded(clone);

        return clone;
    }

    @Override
    protected void moveProjectile(final Vector2 target) {
        moveTowards(target.x, target.y);
    }
}