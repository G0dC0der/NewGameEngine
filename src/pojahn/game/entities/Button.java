package pojahn.game.entities;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.Rectangle;
import pojahn.game.core.Collisions;
import pojahn.game.core.MobileEntity;
import pojahn.game.essentials.Direction;
import pojahn.game.events.Event;

public class Button extends SolidPlatform {

    private Direction pushingDirection;
    private Event pushEvent;
    private Rectangle reuse;
    private Sound pushSound;
    private boolean pushed;

    public Button(float x, float y, MobileEntity... subjects) {
        super(x, y, subjects);
        pushingDirection = Direction.S;
    }

    public void setPushingDirection(Direction pushingDirection){
        if(Direction.isDiagonal(pushingDirection))
            throw new IllegalArgumentException("The pushing direction must be non diagonal: " + pushingDirection);

        this.pushingDirection = pushingDirection;
    }

    public void setPushEvent(Event pushEvent) {
        this.pushEvent = pushEvent;
    }

    public void setPushSound(Sound pushSound) {
        this.pushSound = pushSound;
    }

    @Override
    public void logics() {
        super.logics();

        if(!pushed){
            for(MobileEntity subject : subjects){
                if(Collisions.rectanglesCollide(subject.bounds.toRectangle(), getDummy())){
                    pushed = true;
                    if(pushSound != null)
                        pushSound.play(sounds.calc());
                    if(pushEvent != null)
                        pushEvent.eventHandling();
                    setWaypoint();
                    break;
                }
            }
        }
    }

    private void setWaypoint() {
        switch (pushingDirection){
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

    private Rectangle getDummy(){
        switch (pushingDirection){
            case N:
                reuse.x = x();
                reuse.y = y() + height();
                reuse.width = width();
                reuse.height = 2;
                break;
            case S:
                reuse.x = x();
                reuse.y = y() - 2;
                reuse.width = width();
                reuse.height = 4;
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
