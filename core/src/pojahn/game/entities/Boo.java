package pojahn.game.entities;

import com.badlogic.gdx.audio.Sound;
import pojahn.game.core.BaseLogic;
import pojahn.game.core.MobileEntity;
import pojahn.game.essentials.Animation;
import pojahn.game.essentials.Direction;
import pojahn.game.essentials.Image2D;
import pojahn.lang.Obj;

import java.util.List;

import static com.google.common.collect.ImmutableList.toImmutableList;

public class Boo extends MobileEntity {

    private final List<MobileEntity> victims;
    private Animation<Image2D> hideImage, huntImage;
    private Sound detectSound, hideSound;
    private boolean resetHideImage, resetHuntImage, hunting, ignoreInactive;
    private float velocity, acc, maxSpeed;

    public Boo(final float x, final float y, final MobileEntity... victims) {
        move(x, y);
        this.victims = Obj.requireNotEmpty(victims);
        maxSpeed = 3;
        acc = 0.03f;
        ignoreInactive = true;
    }

    @Override
    public void logistics() {
        final List<MobileEntity> targets = getTargets();
        final MobileEntity victim = !targets.isEmpty() ? (MobileEntity) BaseLogic.findClosest(this, targets) : null;

        if (victim != null && canSneak(victim)) {
            if (maxSpeed > velocity + acc)
                velocity += acc;

            setMoveSpeed(velocity);
            moveTowards(victim.centerX() - halfWidth(), victim.centerY() - halfHeight());

            if (!hunting) {
                sounds.play(detectSound);
                if (resetHuntImage)
                    huntImage.reset();

                super.setImage(huntImage);
            }

            hunting = true;
        } else {
            if (hunting) {
                sounds.play(hideSound);
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
                velocity = 0;
                if (mobile.hasActionEvent()) {
                    mobile.runActionEvent(this);
                }
            }
        }
    }

    private List<MobileEntity> getTargets() {
        return victims.stream()
                .filter(mobileEntity -> !ignoreInactive || mobileEntity.isActive())
                .collect(toImmutableList());
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

    public void setAcc(final float acc) {
        this.acc = acc;
    }

    public void setMaxSpeed(final float maxSpeed) {
        this.maxSpeed = maxSpeed;
    }

    private boolean canSneak(final MobileEntity victim) {
        final Direction victimFacing = victim.getFacing();

        final boolean toTheLeft = x() + halfWidth() > victim.x();
        if (toTheLeft && (victimFacing == Direction.NW || victimFacing == Direction.W || victimFacing == Direction.SW))
            return true;
        if (!toTheLeft && (victimFacing == Direction.NE || victimFacing == Direction.E || victimFacing == Direction.SE))
            return true;

        return false;
    }
}
