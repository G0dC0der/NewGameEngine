package pojahn.game.entities;

import pojahn.game.core.Collisions;
import pojahn.game.core.Entity;
import pojahn.game.essentials.CameraEffects;

public class RemoteVibration extends Entity {

    private Entity[] targets;
    private float vib, radius;
    private int duration;

    public RemoteVibration(Entity... targets) {
        this.targets = targets;
        vib  = 3;
        duration = 25;
        radius = 600;
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

    @Override
    public void logistics() {
        Entity entity = Collisions.findClosest(this, targets);

        if(entity != null) {
            float dist = dist(entity);

            if(radius > dist) {
                float strength = vib;

                getLevel().temp(CameraEffects.vibration(strength), duration);
                getLevel().discard(this);
            }
        }
    }
}
