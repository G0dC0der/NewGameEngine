package pojahn.game.entities.enemy;

import com.badlogic.gdx.audio.Sound;
import pojahn.game.core.BaseLogic;
import pojahn.game.core.MobileEntity;
import pojahn.game.essentials.Animation;
import pojahn.game.essentials.Image2D;
import pojahn.game.events.Event;
import pojahn.lang.Obj;

import java.util.List;

public class JumpingThwump extends MobileEntity {

    public float mass, gravity, damping;

    private int chillFrames, chillCounter;
    private float jumpStrength, vy, tvy, delta;
    private final List<MobileEntity> targets;
    private Animation<Image2D> chillImage, jumpImage;
    private Event slamEvent;
    private Sound slamSound, jumpSound;

    public JumpingThwump(final float x, final float y, final MobileEntity... targets) {
        move(x, y);
        this.targets = Obj.requireNotEmpty(targets);
        chillFrames = 260;
        jumpStrength = 900;

        chillCounter = -1;
        tvy = -800;
        mass = 1.0f;
        gravity = -1000;
        damping = 0.0001f;
    }

    @Override
    public JumpingThwump getClone() {
        final JumpingThwump clone = new JumpingThwump(x(), y(), targets.toArray(new MobileEntity[targets.size()]));
        if (cloneEvent != null)
            cloneEvent.handleClonded(clone);

        copyData(clone);
        return clone;
    }

    @Override
    public void init() {
        delta = getEngine().delta;
        targets.forEach(mobileEntity -> mobileEntity.addObstacle(this));
    }

    @Override
    public void logistics() {
        if (++chillCounter % chillFrames == 0) {
            vy = jumpStrength;

            sounds.play(jumpSound);
            if (jumpImage != null)
                super.setImage(jumpImage);
        }

        if (vy != 0.0f)
            drag();

        final float futureY = getFutureY();
        if (!occupiedAt(x(), futureY))
            applyYForces();
        else {
            if (vy < 0) {
                tryDown(10);

                sounds.play(slamSound);

                if (slamEvent != null)
                    slamEvent.eventHandling();

                super.setImage(chillImage);
            }

            vy = 0;
        }

        final float scanSize = 1;
        final float x = x() - scanSize;
        final float y = y() - scanSize;
        final float w = width() + scanSize * 2;
        final float h = height() + scanSize * 2;

        for (final MobileEntity sub : targets) {
            if (BaseLogic.rectanglesCollide(x, y, w, h, sub.x(), sub.y(), sub.width(), sub.height())) {

                final float nextX = sub.x() + (x() - prevX());
                final float nextY = sub.y() + (y() - prevY());

                if (!sub.occupiedAt(nextX, nextY))
                    sub.move(nextX, nextY);

                if (BaseLogic.rectanglesCollide(bounds.toRectangle(), sub.bounds.toRectangle()))
                    collisionResponse(sub);
            }
        }
    }

    public void setJumpImage(final Animation<Image2D> jumpImage) {
        this.jumpImage = jumpImage;
    }

    @Override
    public void setImage(final Animation<Image2D> image) {
        super.setImage(image);
        this.chillImage = image;
    }

    public void setChillFrames(final int chillFrames) {
        this.chillFrames = chillFrames;
    }

    public void setJumpStrength(final float jumpStrength) {
        this.jumpStrength = jumpStrength;
    }

    public void setJumpSound(final Sound jumpSound) {
        this.jumpSound = jumpSound;
    }

    public void setSlamSound(final Sound slamSound) {
        this.slamSound = slamSound;
    }

    public void setSlamEvent(final Event slamEvent) {
        this.slamEvent = slamEvent;
    }

    @Override
    public void dispose() {
        targets.forEach(sub -> sub.removeObstacle(this));
    }

    private void drag() {
        final float force = mass * gravity;
        vy *= 1.0 - (damping * delta);

        if (tvy < vy) {
            vy += (force / mass) * delta;
        } else
            vy -= (force / mass) * delta;
    }

    private float getFutureY() {
        return bounds.pos.y - vy * delta;
    }

    private void applyYForces() {
        bounds.pos.y -= vy * delta;
    }

    protected void copyData(final JumpingThwump clone) {
        super.copyData(clone);
        clone.mass = mass;
        clone.gravity = gravity;
        clone.tvy = tvy;
        clone.damping = damping;
        clone.chillFrames = chillFrames;
        clone.delta = delta;
        clone.jumpStrength = jumpStrength;
        clone.slamSound = slamSound;
        clone.jumpSound = jumpSound;
        if (jumpImage != null)
            clone.jumpImage = jumpImage.getClone();
        if (chillImage != null)
            clone.chillImage = chillImage.getClone();
    }
}
