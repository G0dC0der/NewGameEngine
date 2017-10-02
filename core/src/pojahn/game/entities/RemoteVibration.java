package pojahn.game.entities;

import pojahn.game.core.Collisions;
import pojahn.game.core.Entity;
import pojahn.game.essentials.CameraEffects;

@Deprecated
public class RemoteVibration extends Entity {

    private Entity targets[];
    private float vib, radius;
    private int duration;

    public RemoteVibration(final Entity... targets) {
        this.targets = targets;
        vib = 3;
        duration = 45;
        radius = 600;
    }

    @Override
    public RemoteVibration getClone() {
        final RemoteVibration clone = new RemoteVibration(targets);
        copyData(clone);
        if (cloneEvent != null)
            cloneEvent.handleClonded(clone);

        return clone;
    }

    public void setVib(final float vib) {
        this.vib = vib;
    }

    public void setRadius(final float radius) {
        this.radius = radius;
    }

    public void setDuration(final int duration) {
        this.duration = duration;
    }

    public void vibrate(final Entity vibProducer) {
        final Entity entity = Collisions.findClosest(vibProducer, targets);

        if (entity != null) {
            final float dist = vibProducer.dist(entity);

            if (radius > dist) {
                final float strength = vib / dist;

                getLevel().temp(CameraEffects.vibration(strength * 3), duration);
            }
        }
    }

    protected void copyData(final RemoteVibration clone) {
        super.copyData(clone);
        clone.vib = vib;
        clone.radius = radius;
        clone.duration = duration;
    }
}
