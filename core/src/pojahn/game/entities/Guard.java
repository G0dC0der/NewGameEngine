package pojahn.game.entities;

import com.badlogic.gdx.audio.Sound;
import pojahn.game.core.BaseLogic;
import pojahn.game.core.Entity;
import pojahn.lang.Obj;

import java.util.List;
import java.util.stream.Collectors;

public class Guard extends PathDrone {

    private final List<Entity> targets;
    private Sound detectSound;
    private boolean allowSound, ignoreInactive;

    public Guard(final float x, final float y, final Entity... targets) {
        super(x, y);
        this.targets = Obj.requireNotEmpty(targets);
        this.allowSound = ignoreInactive = true;
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
        final Entity target = BaseLogic.findClosestSeeable(
                this,
                targets.stream()
                    .filter(entity -> !ignoreInactive || entity.isActive())
                    .collect(Collectors.toList()));

        if (target != null) {
            moveTowards(target.x(), target.y());
            if (allowSound && detectSound != null)
                sounds.play(detectSound);
            allowSound = false;
        } else {
            super.logistics();
            allowSound = true;
        }
    }
}
