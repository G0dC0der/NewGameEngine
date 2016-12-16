package pojahn.game.entities;

import com.badlogic.gdx.audio.Sound;

import pojahn.game.core.Collisions;
import pojahn.game.core.Entity;
import pojahn.game.core.MobileEntity;
import pojahn.game.essentials.Animation;
import pojahn.game.essentials.Direction;
import pojahn.game.essentials.Image2D;

import java.util.stream.Stream;

public class Boo extends MobileEntity {

    private MobileEntity[] victims;
    private Animation<Image2D> hideImage, huntImage;
    private Sound detectSound, hideSound;
    private boolean resetHideImage, resetHuntImage;
    private boolean hunting, ignoreInactive;
    private float velocity;
    public float acc, maxSpeed;

    public Boo(float x, float y, MobileEntity... victims) {
        super();
        move(x, y);
        this.victims = victims;
        maxSpeed = 3;
        acc = 0.03f;
        ignoreInactive = true;
    }

    @Override
    public void logistics() {
        MobileEntity[] targets = getTargets();
        MobileEntity victim = (MobileEntity) Collisions.findClosest(this, targets);

        if (canSneak(victim)) {
            if (maxSpeed > velocity + acc)
                velocity += acc;

            setMoveSpeed(velocity);
            moveTowards(victim.centerX() - halfWidth(), victim.centerY() - halfHeight());

            if (!hunting) {
                if (detectSound != null)
                    detectSound.play(sounds.calc());
                if (resetHuntImage)
                    huntImage.reset();

                super.setImage(huntImage);
            }

            hunting = true;
        } else {
            if (hunting) {
                if (hideSound != null)
                    hideSound.play(sounds.calc());
                if (hideImage != null) {
                    if (resetHideImage)
                        hideImage.reset();

                    super.setImage(hideImage);
                }
            }

            velocity = 0;
            hunting = false;
        }

        for (MobileEntity mobile : targets) {
            if (collidesWith(mobile)) {
                if(mobile.hasActionEvent())
                    mobile.runActionEvent(this);
                velocity = 0;
            }
        }
    }

    private MobileEntity[] getTargets() {
        return Stream.of(victims)
                .filter(mobileEntity -> !ignoreInactive || mobileEntity.isActive())
                .toArray(MobileEntity[]::new);
    }

    @Override
    public void setImage(Animation<Image2D> obj) {
        super.setImage(obj);
        huntImage = obj;
    }

    public void setIgnoreInactive(boolean ignoreInactive) {
        this.ignoreInactive = ignoreInactive;
    }

    public void setHuntImage(Animation<Image2D> huntImage) {
        setImage(huntImage);
    }

    public void setHideImage(Animation<Image2D> hideImage) {
        this.hideImage = hideImage;
    }

    public void setDetectSound(Sound sound) {
        detectSound = sound;
    }

    public void setHideSound(Sound hideSound) {
        this.hideSound = hideSound;
    }

    public void resetHideImage(boolean reset) {
        this.resetHideImage = reset;
    }

    public void resetHuntImage(boolean reset) {
        this.resetHuntImage = reset;
    }

    public boolean isHunting() {
        return hunting;
    }

    private boolean canSneak(MobileEntity victim) {
        Direction victimFacing = victim.getFacing();

        boolean toTheLeft = x() + width() / 2 > victim.x();
        if( toTheLeft && (victimFacing == Direction.NW || victimFacing == Direction.W || victimFacing == Direction.SW))
            return true;
        if(!toTheLeft && (victimFacing == Direction.NE || victimFacing == Direction.E || victimFacing == Direction.SE))
            return true;

        return false;
    }
}
