package pojahn.game.entities;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.Vector2;
import pojahn.game.core.BaseLogic;
import pojahn.game.core.Entity;
import pojahn.game.core.MobileEntity;
import pojahn.game.essentials.Animation;
import pojahn.game.essentials.Image2D;
import pojahn.lang.Obj;

import java.util.List;

import static pojahn.game.core.BaseLogic.normalize;

public class EvilDog extends MobileEntity {

    public float thrust, drag, delta, vx, vy;
    private final List<Entity> targets;
    private float maxDistance;
    private boolean hunting;
    private int soundDelay, soundCounter;
    private Animation<Image2D> idleImg, huntImg;
    private Sound hitSound;

    public EvilDog(final float x, final float y, final float maxDistance, final Entity... targets) {
        move(x, y);
        this.maxDistance = maxDistance;
        this.targets = Obj.requireNotEmpty(targets);
        thrust = 500f;
        drag = .5f;
        delta = 1f / 60f;
        soundDelay = 20;
    }

    public void setCollisionSound(final Sound sound) {
        hitSound = sound;
    }

    public void setCollisionSoundDelay(final int delay) {
        soundDelay = delay;
    }

    public boolean isHunting() {
        return hunting;
    }

    public void setMaxDistance(final float maxDistance) {
        this.maxDistance = maxDistance;
    }

    public void idleImage(final Animation<Image2D> idleImg) {
        this.idleImg = idleImg;
    }

    @Override
    public void setImage(final Animation<Image2D> obj) {
        huntImg = obj;
        super.setImage(obj);
    }

    @Override
    public void logistics() {
        ++soundCounter;

        if (!isFrozen()) {
            final Entity closest = BaseLogic.findClosest(this, targets);

            if (maxDistance < 0 || maxDistance > BaseLogic.distance(this, closest)) {

                final Vector2 norP = normalize(closest, this);

                final float accX = thrust * norP.x - drag * vx;
                final float accY = thrust * norP.y - drag * vy;

                vx += delta * accX;
                vy += delta * accY;

                bounds.pos.x += delta * vx;
                bounds.pos.y += delta * vy;

                if (!hunting) {
                    setImage(huntImg);
                }

                hunting = true;
            } else {
                vx = vy = 0;
                if (hunting) {
                    if (idleImg != null)
                        super.setImage(idleImg);
                }
                hunting = false;
            }

            targets.stream()
                .filter(this::collidesWith)
                .forEach(entity -> {
                    if (entity.hasActionEvent())
                        entity.runActionEvent(this);
                    if (hitSound != null && soundCounter % soundDelay == 0)
                        sounds.play(hitSound);
                });
        }
    }
}
