package pojahn.game.entities;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.Vector2;

import pojahn.game.core.Collisions;
import pojahn.game.core.Entity;
import pojahn.game.core.MobileEntity;
import pojahn.game.essentials.Animation;
import pojahn.game.essentials.Image2D;

import java.util.stream.Stream;

import static pojahn.game.core.Collisions.normalize;

public class EvilDog extends MobileEntity {

    public float thrust, drag, delta, vx, vy;
    private float maxDistance;
    private boolean hunting;
    private int soundDelay, soundCounter;
    private Entity[] targets;
    private Animation<Image2D> idleImg, huntImg;
    private Sound hitSound;

    public EvilDog(float x, float y, float maxDistance, Entity... targets) {
        move(x, y);
        this.maxDistance = maxDistance;
        this.targets = targets;
        thrust = 500f;
        drag = .5f;
        delta = 1f / 60f;
        soundDelay = 20;
    }

    public void setCollisionSound(Sound sound) {
        hitSound = sound;
    }

    public void setCollisionSoundDelay(int delay) {
        soundDelay = delay;
    }

    public boolean isHunting() {
        return hunting;
    }

    public void setMaxDistance(float maxDistance) {
        this.maxDistance = maxDistance;
    }

    public void idleImage(Animation<Image2D> idleImg) {
        this.idleImg = idleImg;
    }

    @Override
    public void setImage(Animation<Image2D> obj) {
        huntImg = obj;
        super.setImage(obj);
    }

    @Override
    public void logistics() {
        ++soundCounter;

        if (!isFrozen()) {
            Entity closest = Collisions.findClosest(this, targets);

            if (maxDistance < 0 || maxDistance > Collisions.distance(this, closest)) {

                Vector2 norP = normalize(closest, this);

                float accX = thrust * norP.x - drag * vx;
                float accY = thrust * norP.y - drag * vy;

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

            Stream.of(targets).filter(this::collidesWith).forEach(entity -> {
                if(entity.hasActionEvent())
                    entity.runActionEvent(this);
                if (hitSound != null && soundCounter % soundDelay == 0)
                    hitSound.play(sounds.calc());
            });
        }
    }
}
