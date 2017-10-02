package pojahn.game.entities;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.Vector2;
import pojahn.game.core.Collisions;
import pojahn.game.core.Level;
import pojahn.game.core.MobileEntity;
import pojahn.game.essentials.Direction;

public class SimpleWeapon extends MobileEntity {

    private Direction projDir;
    private Projectile proj;
    private Particle fireAnim;
    private int reloadTime, reloadCounter;
    private float offsetX, offsetY;
    private Sound firingSound;

    public SimpleWeapon(final float x, final float y, final Projectile proj, final Direction projDir, final int reloadTime) {
        move(x, y);
        this.proj = proj;
        this.projDir = projDir;
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

    public void setFiringDirection(final Direction projDir) {
        this.projDir = projDir;
    }

    public void setFiringSound(final Sound sound) {
        firingSound = sound;
    }

    @Override
    public void logistics() {
        if (--reloadCounter < 0) {
            final Level l = getLevel();
            final float startX = bounds.pos.x + offsetX;
            final float startY = bounds.pos.y + offsetY;

            final Projectile projClone = proj.getClone();
            final Vector2 target = Collisions.getEdgePoint((int) startX, (int) startY, projDir, l);

            projClone.move(startX, startY);
            projClone.setTarget(target.x, target.y);

            if (fireAnim != null)
                l.add(fireAnim.getClone().move(startX, startY));

            l.add(projClone);
            reloadCounter = reloadTime;

            if (firingSound != null)
                firingSound.play(sounds.calc());
        }
    }
}