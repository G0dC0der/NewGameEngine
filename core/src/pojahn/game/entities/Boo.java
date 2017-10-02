package pojahn.game.entities;

import com.badlogic.gdx.audio.Sound;
import pojahn.game.core.Collisions;
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

    public Boo(final float x, final float y, final MobileEntity... victims) {
        super();
        move(x, y);
        this.victims = victims;
        maxSpeed = 3;
        acc = 0.03f;
        ignoreInactive = true;
    }

    @Override
    public void logistics() {
        final MobileEntity[] targets = getTargets();
        final MobileEntity victim = targets.length > 0 ? (MobileEntity) Collisions.findClosest(this, targets) : null;

        if (victim != null && canSneak(victim)) {
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

        for (final MobileEntity mobile : targets) {
            if (collidesWith(mobile)) {
                if (mobile.hasActionEvent())
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
    public void setImage(final Animation<Image2D> obj) {
        super.setImage(obj);
        huntImage = obj;
    }

    public void setIgnoreInactive(final boolean ignoreInactive) {
        this.ignoreInactive = ignoreInactive;
    }

    public void setHuntImage(final Animation<Image2D> huntImage) {
        setImage(huntImage);
    }

    public void setHideImage(final Animation<Image2D> hideImage) {
        this.hideImage = hideImage;
    }

    public void setDetectSound(final Sound sound) {
        detectSound = sound;
    }

    public void setHideSound(final Sound hideSound) {
        this.hideSound = hideSound;
    }

    public void resetHideImage(final boolean reset) {
        this.resetHideImage = reset;
    }

    public void resetHuntImage(final boolean reset) {
        this.resetHuntImage = reset;
    }

    public boolean isHunting() {
        return hunting;
    }

    private boolean canSneak(final MobileEntity victim) {
        final Direction victimFacing = victim.getFacing();

        final boolean toTheLeft = x() + width() / 2 > victim.x();
        if (toTheLeft && (victimFacing == Direction.NW || victimFacing == Direction.W || victimFacing == Direction.SW))
            return true;
        if (!toTheLeft && (victimFacing == Direction.NE || victimFacing == Direction.E || victimFacing == Direction.SE))
            return true;

        return false;
    }
}
