package pojahn.game.desktop.redguyruns.levels.hill;

import pojahn.game.core.Entity;
import pojahn.game.core.Level;
import pojahn.game.entities.Bullet;

public class FallingProjectile extends Bullet {

    private Entity parent, targets[];
    private Level level;

    public FallingProjectile(final float x, final float y, final Entity parent, final Level level, final Entity... scanTargets) {
        super(x, y, scanTargets);
        this.parent = parent;
        this.targets = scanTargets;
        this.level = level;
        setTarget(parent.x(), level.getHeight());
    }

    @Override
    public FallingProjectile getClone() {
        final FallingProjectile clone = new FallingProjectile(x(), y(), parent, level, targets);
        copyData(clone);

        if (cloneEvent != null) {
            cloneEvent.handleClonded(clone);
        }

        return clone;
    }
}
