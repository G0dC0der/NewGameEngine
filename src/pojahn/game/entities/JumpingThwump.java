package pojahn.game.entities;

import com.badlogic.gdx.audio.Sound;
import pojahn.game.core.Collisions;
import pojahn.game.core.MobileEntity;
import pojahn.game.essentials.Animation;
import pojahn.game.essentials.Image2D;
import pojahn.game.events.Event;

import java.util.stream.Stream;

public class JumpingThwump extends MobileEntity {

    public float mass, gravity, damping;

    private int chillFrames, chillCounter;
    private float jumpStrength, vy, tvy, delta;
    private MobileEntity[] targets;
    private Animation<Image2D> chillImage, jumpImage;
    private Event slamEvent;
    private Sound slamSound, jumpSound;

    public JumpingThwump(float x, float y, MobileEntity... targets) {
        move(x,y);
        this.targets = targets;
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
        JumpingThwump clone = new JumpingThwump(x(), y(), targets);
        if (cloneEvent != null)
            cloneEvent.handleClonded(clone);

        copyData(clone);
        return clone;
    }

    @Override
    public void init() {
        delta = getEngine().delta;
        Stream.of(targets).forEach(mobileEntity -> mobileEntity.addObstacle(this));
    }

    @Override
    public void logistics() {
        if (++chillCounter % chillFrames == 0) {
            vy = jumpStrength;

            if (jumpSound != null)
                jumpSound.play(sounds.calc());
            if (jumpImage != null)
                super.setImage(jumpImage);
        }

        if (vy != 0.0f)
            drag();

        float futureY = getFutureY();
        if (!occupiedAt(x(), futureY))
            applyYForces();
        else {
            if (vy < 0) {
                tryDown(10);
                if (slamSound != null)
                    slamSound.play(sounds.calc());
                if (slamEvent != null)
                    slamEvent.eventHandling();

                super.setImage(chillImage);
            }

            vy = 0;
        }

        float scanSize = 1;
        float x = x() - scanSize;
        float y = y() - scanSize;
        float w = width() + scanSize * 2;
        float h = height() + scanSize * 2;

        for (MobileEntity sub : targets) {
            if (Collisions.rectanglesCollide(x, y, w, h, sub.x(), sub.y(), sub.width(), sub.height())) {

                float nextX = sub.x() + (x() - prevX());
                float nextY = sub.y() + (y() - prevY());

                if (!sub.occupiedAt(nextX, nextY))
                    sub.move(nextX, nextY);

                if (Collisions.rectanglesCollide(bounds.toRectangle(), sub.bounds.toRectangle()))
                    collisionResponse(sub);
            }
        }
    }

    public void setJumpImage(Animation<Image2D> jumpImage) {
        this.jumpImage = jumpImage;
    }

    @Override
    public void setImage(Animation<Image2D> image) {
        super.setImage(image);
        this.chillImage = image;
    }

    public void setChillFrames(int chillFrames) {
        this.chillFrames = chillFrames;
    }

    public void setJumpStrength(float jumpStrength) {
        this.jumpStrength = jumpStrength;
    }

    public void setJumpSound(Sound jumpSound) {
        this.jumpSound = jumpSound;
    }

    public void setSlamSound(Sound slamSound) {
        this.slamSound = slamSound;
    }

    public void setSlamEvent(Event slamEvent) {
        this.slamEvent = slamEvent;
    }

    @Override
    public void dispose() {
        if (targets != null) {
            Stream.of(targets).forEach(sub -> sub.removeObstacle(this));
        }
    }

    private void drag() {
        float force = mass * gravity;
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

    protected void copyData(JumpingThwump clone) {
        super.copyData(clone);
        clone.mass = mass;
        clone.gravity = gravity;
        clone.tvy = tvy;
        clone.damping = damping;
        clone.chillFrames = chillFrames;
        clone.delta = delta;
        clone.jumpStrength = jumpStrength;
        clone.slamSound =slamSound;
        clone.jumpSound = jumpSound;
        if (jumpImage != null)
            clone.jumpImage = jumpImage.getClone();
        if (chillImage != null)
            clone.chillImage = chillImage.getClone();
    }
}
