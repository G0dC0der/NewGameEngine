package pojahn.game.essentials;

import com.badlogic.gdx.math.Vector2;
import pojahn.game.core.BaseLogic;
import pojahn.game.core.Entity;

import java.util.Objects;
import java.util.function.Supplier;

public abstract class Checkpoint {

    private boolean taken;
    private final Vector2 start;

    public Checkpoint(final Vector2 start) {
        this.start = Objects.requireNonNull(start);
    }

    public abstract boolean reached(final Entity entity);

    public void take() {
        taken = true;
    }

    public void reset() {
        taken = false;
    }

    public boolean isTaken() {
        return taken;
    }

    public Vector2 getStart() {
        return new Vector2(start);
    }

    public static class AreaCheckpoint extends Checkpoint {
        private final float x, y, width, height;

        public AreaCheckpoint(final float startX, final float startY, final float x, final float y, final float width, final float height) {
            super(new Vector2(startX, startY));
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        @Override
        public boolean reached(final Entity entity) {
            return BaseLogic.rectanglesCollide(entity.x(), entity.y(), entity.width(), entity.height(), x, y, width, height);
        }
    }

    public static class EventCheckpoint extends Checkpoint {
        private final Supplier<Boolean> supplier;

        public EventCheckpoint(final Vector2 start, final Supplier<Boolean> supplier) {
            super(start);
            this.supplier = supplier;
        }

        @Override
        public boolean reached(final Entity entity) {
            return supplier.get();
        }
    }
}