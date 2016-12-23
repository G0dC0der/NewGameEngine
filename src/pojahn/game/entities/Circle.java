package pojahn.game.entities;

import pojahn.game.core.MobileEntity;

public class Circle extends MobileEntity {

    private float centerX, centerY, radius, counter;

    /**
     * Creates an entity which circles the given point.
     *
     * @param centerX       The center X coordinate.
     * @param centerY       The center Y coordinate.
     * @param radius        The radius, measured in pixels.
     * @param startingAngle The starting angle in radians.
     */
    public Circle(float centerX, float centerY, float radius, float startingAngle) {
        this.centerX = centerX;
        this.centerY = centerY;
        this.radius = radius;
        counter = startingAngle;
    }

    @Override
    public void logistics() {
        counter += getMoveSpeed();

        bounds.pos.x = (float) (radius * Math.cos(counter) + centerX);
        bounds.pos.y = (float) (radius * Math.sin(counter) + centerY);
    }

    public void setCenterX(float centerX) {
        this.centerX = centerX;
    }

    public void setCenterY(float centerY) {
        this.centerY = centerY;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }
}