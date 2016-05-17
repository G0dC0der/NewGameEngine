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

    public SimpleWeapon(float x, float y, Projectile proj, Direction projDir, int reloadTime) {
        move(x, y);
        this.proj = proj;
        this.projDir = projDir;
        this.reloadTime = reloadTime;
    }

    public void spawnOffset(float offsetX, float offsetY) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }

    public void setFiringAnimation(Particle fireAnim) {
        this.fireAnim = fireAnim;
    }

    public boolean reloading() {
        return reloadCounter > 0;
    }

    public void setFiringDirection(Direction projDir) {
        this.projDir = projDir;
    }

    public void setFiringSound(Sound sound) {
        firingSound = sound;
    }

    @Override
    public void logistics() {
        if (--reloadCounter < 0) {
            Level l = getLevel();
            float startX = bounds.pos.x + offsetX;
            float startY = bounds.pos.y + offsetY;

            Projectile projClone = proj.getClone();
            Vector2 target = Collisions.getEdgePoint((int) startX, (int) startY, projDir, l);

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

    private Vector2 getTarget(float startX, float startY) {
        switch (projDir) {
            case N:
                startY = 0;
                break;
        }

        return new Vector2(startX, startY);
    }
}