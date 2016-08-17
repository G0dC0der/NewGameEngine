package pojahn.game.entities;

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

    public LineMovement(Movement movement) {
        if (movement == null)
            throw new IllegalArgumentException("Must set a movement.");

        this.movement = movement;
    }

    @Override
    public LineMovement getClone() {
        LineMovement clone = new LineMovement(movement);
        copyData(clone);
        if (cloneEvent != null)
            cloneEvent.handleClonded(clone);

        return clone;
    }

    public void setMovement(Movement movement) {
        this.movement = movement;
    }

    public void setSlamSound(Sound slamSound) {
        this.slamSound = slamSound;
    }

    @Override
    public void logistics() {
        Vector2 next;

        if (movement == Movement.HORIZONTAL)
            next = attemptTowards(leftOrUp ? 0 : getLevel().getWidth(), y(), getMoveSpeed());
        else
            next = attemptTowards(x(), leftOrUp ? 0 : getLevel().getHeight(), getMoveSpeed());

        if (!occupiedAt(next.x, next.y))
            move(next);
        else {
            leftOrUp = !leftOrUp;
            if (slamSound != null)
                slamSound.play(sounds.calc());
        }
    }
}
