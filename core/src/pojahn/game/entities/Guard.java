package pojahn.game.entities;

import com.badlogic.gdx.audio.Sound;
import pojahn.game.core.Collisions;
import pojahn.game.core.Entity;

import java.util.stream.Stream;

public class Guard extends PathDrone {

    private Entity[] targets;
    private Sound detectSound;
    private boolean allowSound, ignoreInactive;

    public Guard(final float x, final float y, final Entity... targets) {
        super(x, y);
        this.targets = targets;
        allowSound = ignoreInactive = true;
    }

    public boolean isHunting() {
        return !allowSound;
    }

    public void setDetectSound(final Sound detectSound) {
        this.detectSound = detectSound;
    }

    public void setIgnoreInactive(final boolean ignoreInactive) {
        this.ignoreInactive = ignoreInactive;
    }

    @Override
    public void logistics() {
        final Entity target = Collisions.findClosestSeeable(this, Stream.of(targets).filter(entity -> !ignoreInactive || entity.isActive()).toArray(Entity[]::new));

        if (target != null) {
            moveTowards(target.x(), target.y());
            if (allowSound && detectSound != null)
                detectSound.play(sounds.calc());
            allowSound = false;
        } else {
            super.logistics();
            allowSound = true;
        }
    }
}
