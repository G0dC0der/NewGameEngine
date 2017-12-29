package pojahn.game.entities.movement;

import pojahn.game.events.Event;

import java.io.Serializable;
import java.util.function.Supplier;

public interface Waypoint {

    float targetX();

    float getTargetY();

    int freezeFrames();

    boolean jump();

    Event getEvent();

    class StaticWaypoint implements Serializable, Waypoint {
        private final float x, y;
        private final int frames;
        private final boolean jump;
        private final Event event;

        public StaticWaypoint(final float x, final float y, final int frames, final boolean jump, final Event event) {
            this.x = x;
            this.y = y;
            this.frames = frames;
            this.jump = jump;
            this.event = event;
        }

        public StaticWaypoint(final float x, final float y) {
            this(x, y, 0, false, null);
        }

        @Override
        public float targetX() {
            return x;
        }

        @Override
        public float getTargetY() {
            return y;
        }

        @Override
        public int freezeFrames() {
            return frames;
        }

        @Override
        public boolean jump() {
            return jump;
        }

        @Override
        public Event getEvent() {
            return event;
        }

        @Override
        public String toString() {
            return x + ":" + y + " " + frames + " - " + jump;
        }
    }

    class DynamicWaypoint implements Serializable, Waypoint {
        private final Supplier<Float> xSupplier;
        private final Supplier<Float> ySupplier;
        private final Supplier<Integer> framesSupplier;
        private final Supplier<Boolean> jumpSupplier;
        private final Event event;

        public DynamicWaypoint(final Supplier<Float> xSupplier,
                               final Supplier<Float> ySupplier,
                               final Supplier<Integer> framesSupplier,
                               final Supplier<Boolean> jumpSupplier,
                               final Event event) {
            this.xSupplier = xSupplier;
            this.ySupplier = ySupplier;
            this.framesSupplier = framesSupplier;
            this.jumpSupplier = jumpSupplier;
            this.event = event;
        }

        public DynamicWaypoint(final Supplier<Float> xSupplier, final Supplier<Float> ySupplier, final Event event) {
            this(xSupplier, ySupplier, ()-> 0, ()-> false, event);
        }

        public DynamicWaypoint(final Supplier<Float> xSupplier, final Supplier<Float> ySupplier) {
            this(xSupplier, ySupplier, ()-> 0, ()-> false, null);
        }

        @Override
        public float targetX() {
            return xSupplier.get();
        }

        @Override
        public float getTargetY() {
            return ySupplier.get();
        }

        @Override
        public int freezeFrames() {
            return framesSupplier.get();
        }

        @Override
        public boolean jump() {
            return jumpSupplier.get();
        }

        @Override
        public Event getEvent() {
            return event;
        }
    }
}
