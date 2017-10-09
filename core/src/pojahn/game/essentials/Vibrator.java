package pojahn.game.essentials;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.Vector2;
import com.google.common.collect.ImmutableList;
import pojahn.game.core.BaseLogic;
import pojahn.game.core.Entity;
import pojahn.game.core.Level;
import pojahn.lang.Obj;

import java.util.List;

public class Vibrator {

    public enum VibDirection {
        LEFT,
        RIGHT,
        TOP,
        BOTTOM,
        CENTER
    }

    private final Level level;
    private final Entity producer;
    private final List<Entity> consumers;
    private final VibDirection vibDirection;
    private Sound vibrateSound;
    private float strength, radius;
    private int duration;
    private boolean staticStrength;

    public Vibrator(final Level level) {
        this.level = level;
        producer = null;
        consumers = null;
        vibDirection = null;
    }

    public Vibrator(final Level level, final Entity producer, final VibDirection vibDirection, final Entity... consumers) {
        this.level = Obj.nonNull(level);
        this.producer = Obj.nonNull(producer);
        this.consumers = Obj.requireNotEmpty(consumers);
        this.vibDirection = Obj.nonNull(vibDirection);
        strength = 3;
        duration = 45;
        radius = 600;
    }

    public void setStrength(final float strength) {
        this.strength = strength;
    }

    public void setDuration(final int duration) {
        this.duration = duration;
    }

    public void setRadius(final float radius) {
        this.radius = radius;
    }

    public void setStaticStrength(final boolean staticStrength) {
        this.staticStrength = staticStrength;
    }

    public void setVibrateSound(Sound vibrateSound) {
        this.vibrateSound = vibrateSound;
    }

    public void vibrate() {
        vibrate(producer, vibDirection, consumers.toArray(new Entity[0]));
    }

    public void vibrate(final Entity producer, final VibDirection vibDirection, final Entity... consumers) {
        final Entity closest = BaseLogic.findClosest(producer, ImmutableList.copyOf(consumers));
        final Vector2 point = getPoint(producer, vibDirection);
        final float dist = (float) BaseLogic.distance(point.x, point.y, closest.x(), closest.y());

        if (radius > dist) {
            final float power = staticStrength ? strength : strength / dist;
            level.temp(CameraEffects.vibration(power * 3), duration);

            producer.sounds.play(vibrateSound);
        }
    }

    private Vector2 getPoint(final Entity producer, final VibDirection vibDirection) {
        switch (vibDirection) {
            case BOTTOM:
                return new Vector2(producer.centerX(), producer.y() + producer.height());
            case TOP:
                return new Vector2(producer.centerX(), producer.y());
            case LEFT:
                return new Vector2(producer.x(), producer.centerY());
            case RIGHT:
                return new Vector2(producer.x() + producer.width(), producer.centerY());
            case CENTER:
                return new Vector2(producer.centerX(), producer.centerY());
        }

        throw new RuntimeException("Unknown mapping for " + vibDirection);
    }
}
