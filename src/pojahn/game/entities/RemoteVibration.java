package pojahn.game.entities;

import pojahn.game.core.Collisions;
import pojahn.game.core.Entity;
import pojahn.game.essentials.CameraEffects;

public class RemoteVibration extends Entity {

    private Entity targets[];
    private float vib, radius;
    private int duration;

    public RemoteVibration(Entity... targets) {
        this.targets = targets;
        vib  = 3;
        duration = 45;
        radius = 600;
    }

    @Override
    public RemoteVibration getClone() {
        RemoteVibration clone = new RemoteVibration(targets);
        copyData(clone);
        if (cloneEvent != null)
            cloneEvent.handleClonded(clone);

        return clone;
    }

    public void setVib(float vib) {
        this.vib = vib;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public void vibrate(Entity vibProducer) {
        Entity entity = Collisions.findClosest(vibProducer, targets);

        if(entity != null) {
            float dist = vibProducer.dist(entity);

            if(radius > dist) {
                float strength = vib / dist;

                getLevel().temp(CameraEffects.vibration(strength * 3), duration);
            }
        }
    }

    protected void copyData(RemoteVibration clone) {
        super.copyData(clone);
        clone.vib = vib;
        clone.radius = radius;
        clone.duration = duration;
    }
}
