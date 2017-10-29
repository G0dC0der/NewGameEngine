package pojahn.game.entities.object;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.Rectangle;
import pojahn.game.core.BaseLogic;
import pojahn.game.core.MobileEntity;
import pojahn.game.entities.platform.SolidPlatform;
import pojahn.game.essentials.Direction;
import pojahn.game.events.Event;
import pojahn.lang.Obj;

import java.util.List;
import java.util.Objects;

public class Button extends SolidPlatform {

    private final Direction pushingDirection;
    private final List<MobileEntity> subjects;
    private Event pushEvent;
    private Rectangle reuse;
    private Sound pushSound;
    private boolean pushed;

    public Button(final float x, final float y, final Direction pushingDirection, final MobileEntity... subjects) {
        super(x, y, subjects);

        this.subjects = Obj.requireNotEmpty(subjects);
        this.reuse = new Rectangle();
        this.pushingDirection = Objects.requireNonNull(pushingDirection);

        if (pushingDirection.isDiagonal()) {
            throw new IllegalArgumentException("The pushing direction must be non diagonal: " + pushingDirection);
        }
    }

    public void setPushEvent(final Event pushEvent) {
        this.pushEvent = pushEvent;
    }

    public void setPushSound(final Sound pushSound) {
        this.pushSound = pushSound;
    }

    @Override
    public void logistics() {
        super.logistics();

        if (!pushed) {
            for (final MobileEntity subject : subjects) {
                if (BaseLogic.rectanglesCollide(subject.bounds.toRectangle(), getDummy())) {
                    pushed = true;
                    if (pushSound != null)
                        sounds.play(pushSound);
                    if (pushEvent != null)
                        pushEvent.eventHandling();
                    setWaypoint();
                    break;
                }
            }
        }
    }

    private void setWaypoint() {
        switch (pushingDirection) {
            case N:
                appendPath(x(), y() - halfHeight());
                break;
            case S:
                appendPath(x(), y() + halfHeight());
                break;
            case E:
                appendPath(x() + halfWidth(), y());
                break;
            case W:
                appendPath(x() - halfWidth(), y());
                break;
        }
    }

    private Rectangle getDummy() {
        switch (pushingDirection) {
            case N:
                reuse.x = x();
                reuse.y = y() + height();
                reuse.width = width();
                reuse.height = 2;
                break;
            case S:
                reuse.x = x();
                reuse.y = y() - 3;
                reuse.width = width();
                reuse.height = 6;
                break;
            case E:
                reuse.x = x() - 2;
                reuse.y = y();
                reuse.width = 2;
                reuse.height = height();
                break;
            case W:
                reuse.x = x() + width();
                reuse.y = y();
                reuse.width = 2;
                reuse.height = height();
                break;
        }
        return reuse;
    }
}