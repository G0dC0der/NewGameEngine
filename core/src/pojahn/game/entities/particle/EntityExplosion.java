package pojahn.game.entities.particle;

import pojahn.game.core.Entity;
import pojahn.game.entities.enemy.weapon.Projectile;
import pojahn.game.essentials.Animation;
import pojahn.game.essentials.Factory;
import pojahn.game.essentials.Image2D;
import pojahn.game.events.ActionEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class EntityExplosion extends Particle {

    private final Entity entity;
    private final int rows, cols;
    private final List<Animation<Image2D>> parts;
    private float vx, vy, toleranceX, toleranceY;
    private Consumer<Debris> callback;

    public EntityExplosion(final Entity entity, final int rows, final int cols, final List<Animation<Image2D>> parts) {
        if (rows * cols != parts.size()) {
            throw new IllegalArgumentException("The parts must be equal  to rows * cols");
        }

        this.rows = rows;
        this.cols = cols;
        this.parts = parts;
        this.entity = Objects.requireNonNull(entity);

        this.vx = this.vy = 80;
        this.toleranceX = this.toleranceY = 25;
    }

    public void setVx(final float vx) {
        this.vx = vx;
    }

    public void setVy(final float vy) {
        this.vy = vy;
    }

    public void setToleranceX(final float toleranceX) {
        this.toleranceX = toleranceX;
    }

    public void setToleranceY(final float toleranceY) {
        this.toleranceY = toleranceY;
    }

    public void setCallback(final Consumer<Debris> callback) {
        this.callback = callback;
    }

    @Override
    protected void erupt() {
        final float offsetX = entity.width() / cols;
        final float offsetY = entity.height() / rows;

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                final Debris debris = new Debris(vx, toleranceX, vy, toleranceY);
                debris.setImage(parts.get(row * cols + col));
                debris.move(entity.x() + offsetX * col, entity.y() + offsetY * row);
                debris.zIndex(getZIndex());
                if (callback != null) {
                    callback.accept(debris);
                }
                getLevel().add(debris);
            }
        }
    }

    @Override
    protected boolean completed() {
        return true;
    }

    @Override
    public Particle getClone() {
        final EntityExplosion clone = new EntityExplosion(entity, rows, cols, copy());
        copyData(clone);
        if (cloneEvent != null)
            cloneEvent.handleClonded(clone);

        return clone;
    }

    protected void copyData(final EntityExplosion clone) {
        super.copyData(clone);
        clone.vx = vx;
        clone.vy = vy;
        clone.toleranceX = toleranceX;
        clone.toleranceY = toleranceY;
    }

    private List<Animation<Image2D>> copy() {
        final ArrayList<Animation<Image2D>> parts = new ArrayList<>(this.parts.size());
        this.parts.forEach(part -> parts.add(part.getClone()));

        return parts;
    }
}
