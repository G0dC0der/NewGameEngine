package pojahn.game.entities.movement;

import com.badlogic.gdx.math.Vector2;
import pojahn.game.core.MobileEntity;

public class Circle extends MobileEntity {

    private float centerX;
    private float centerY;
    private float radius;
    private float counter;
    private boolean centerize;

    /**
     * Creates an entity which circles the given point.
     *
     * @param centerX       The center X coordinate.
     * @param centerY       The center Y coordinate.
     * @param radius        The radius, measured in pixels.
     * @param startingAngle The starting angle in radians.
     */
    public Circle(final float centerX, final float centerY, final float radius, final float startingAngle) {
        this.centerX = centerX;
        this.centerY = centerY;
        this.radius = radius;
        counter = startingAngle;
    }

    public void setCenter(final Vector2 center) {
        centerX = center.x;
        centerY = center.y;
    }

    public void setCenterize(final boolean centerize) {
        this.centerize = centerize;
    }

    public void setCenterX(final float centerX) {
        this.centerX = centerX;
    }

    public void setCenterY(final float centerY) {
        this.centerY = centerY;
    }

    public void setRadius(final float radius) {
        this.radius = radius;
    }

    public float getRadius() {
        return radius;
    }

    @Override
    public void logistics() {
        if (isFrozen()) {
            return;
        }

        counter += getMoveSpeed();

        bounds.pos.x = (float) (radius * Math.cos(counter) + centerX);
        bounds.pos.y = (float) (radius * Math.sin(counter) + centerY);
        if (centerize) {
            bounds.pos.x -= halfWidth();
            bounds.pos.y -= halfHeight();
        }
    }
}