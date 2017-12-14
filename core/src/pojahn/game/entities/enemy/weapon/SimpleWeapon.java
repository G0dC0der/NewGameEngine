package pojahn.game.entities.enemy.weapon;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.Vector2;
import pojahn.game.core.BaseLogic;
import pojahn.game.core.Level;
import pojahn.game.core.MobileEntity;
import pojahn.game.entities.particle.Particle;
import pojahn.game.essentials.Direction;

import java.util.Objects;

public class SimpleWeapon extends MobileEntity {

    private final Direction projDir;
    private final Projectile proj;
    private final int reloadTime;
    private int reloadCounter;
    private float offsetX, offsetY;
    private Particle fireAnim;
    private Sound firingSound;

    public SimpleWeapon(final float x, final float y, final Projectile proj, final Direction projDir, final int reloadTime) {
        move(x, y);
        this.proj = Objects.requireNonNull(proj);
        this.projDir = Objects.requireNonNull(projDir);
        this.reloadTime = reloadTime;
    }

    @Override
    public SimpleWeapon getClone() {
        final SimpleWeapon clone = new SimpleWeapon(x(), y(), proj, projDir, reloadTime);
        copyData(clone);
        if (cloneEvent != null)
            cloneEvent.handleClonded(clone);

        return clone;
    }

    protected void copyData(final SimpleWeapon clone) {
        super.copyData(clone);
        clone.fireAnim = fireAnim;
        clone.offsetX = offsetX;
        clone.offsetY = offsetY;
        clone.firingSound = firingSound;
    }

    public void spawnOffset(final float offsetX, final float offsetY) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }

    public void setFiringAnimation(final Particle fireAnim) {
        this.fireAnim = fireAnim;
    }

    public boolean reloading() {
        return reloadCounter > 0;
    }

    public void setFiringSound(final Sound sound) {
        firingSound = sound;
    }

    @Override
    public void logistics() {
        if (isFrozen())
            return;

        if (--reloadCounter < 0) {
            final Level l = getLevel();
            final float startX = bounds.pos.x + offsetX;
            final float startY = bounds.pos.y + offsetY;

            final Projectile projClone = proj.getClone();
            final Vector2 target = BaseLogic.getEdgePoint((int) startX, (int) startY, projDir, l);

            projClone.move(startX, startY);
            projClone.setTarget(target.x, target.y);

            if (fireAnim != null)
                l.add(fireAnim.getClone().move(startX, startY));

            l.add(projClone);
            reloadCounter = reloadTime;

            sounds.play(firingSound);
        }
    }
}