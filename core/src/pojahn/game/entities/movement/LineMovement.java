package pojahn.game.entities.movement;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.Vector2;
import pojahn.game.core.MobileEntity;

public class LineMovement extends MobileEntity {

    public enum Movement {
        VERTICAL,
        HORIZONTAL
    }

    private Movement movement;
    private Sound slamSound;
    private boolean leftOrUp;

    public LineMovement(final Movement movement) {
        if (movement == null)
            throw new IllegalArgumentException("Must set a movement.");

        this.movement = movement;
    }

    @Override
    public LineMovement getClone() {
        final LineMovement clone = new LineMovement(movement);
        copyData(clone);
        if (cloneEvent != null)
            cloneEvent.handleClonded(clone);

        return clone;
    }

    public void setMovement(final Movement movement) {
        this.movement = movement;
    }

    public void setSlamSound(final Sound slamSound) {
        this.slamSound = slamSound;
    }

    public void flipMovement() {
        leftOrUp = !leftOrUp;
    }

    @Override
    public void logistics() {
        if (isFrozen())
            return;

        final Vector2 next;

        if (movement == Movement.HORIZONTAL)
            next = attemptTowards(leftOrUp ? 0 : getLevel().getWidth(), y(), getMoveSpeed());
        else
            next = attemptTowards(x(), leftOrUp ? 0 : getLevel().getHeight(), getMoveSpeed());

        if (!occupiedAt(next.x, next.y))
            move(next);
        else {
            leftOrUp = !leftOrUp;
            sounds.play(slamSound);
        }
    }
}
