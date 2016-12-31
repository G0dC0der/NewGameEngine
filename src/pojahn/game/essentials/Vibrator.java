package pojahn.game.essentials;

import com.badlogic.gdx.math.Vector2;
import pojahn.game.core.Collisions;
import pojahn.game.core.Entity;
import pojahn.game.core.Level;

import java.util.stream.Stream;

public class Vibrator {

    private Entity producerEntity,  consumerEntities[];
    private Vector2 producerVector, consumersVector[];
    private Level level;
    private float strength, radius;
    private int duration;
    private boolean centerEntities, staticStrength;

    {
        strength = 3;
        duration = 45;
        radius = 600;
        centerEntities = true;
    }

    public Vibrator(Entity producer, Entity... consumer) {
        producerEntity = producer;
        consumerEntities = consumer;
    }

    public Vibrator(Vector2 producer, Vector2... consumers) {
        this.producerVector = producer;
        this.consumersVector = consumers;
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    public void setStrength(float strength) {
        this.strength = strength;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public void centerEntities(boolean centerEntities) {
        this.centerEntities = centerEntities;
    }

    public void setStaticStrength(boolean staticStrength) {
        this.staticStrength = staticStrength;
    }

    public void vibrate() {
        Vector2 producer;
        Vector2[] consumers;

        if (producerEntity != null) {
            producer = centerEntities ? producerEntity.bounds.center() : producerEntity.bounds.pos;
        } else {
            producer = producerVector;
        }

        if (consumerEntities != null) {
            consumers = Stream
                    .of(consumerEntities)
                    .map(entity -> centerEntities ? entity.bounds.center() : entity.bounds.pos)
                    .toArray(Vector2[]::new);
        } else {
            consumers = consumersVector;
        }

        vib(producer, consumers);
    }

    private void vib(Vector2 producer, Vector2... consumers) {
        Vector2 closest = Collisions.findClosest(producer, consumers);

        if(closest != null) {
            float dist = producer.dst(closest);

            if(radius > dist) {
                float power = staticStrength ? strength : strength / dist;
                level.temp(CameraEffects.vibration(power * 3), duration);
            }
        }

    }
}
