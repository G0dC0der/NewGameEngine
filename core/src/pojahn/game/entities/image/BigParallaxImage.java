package pojahn.game.entities.image;

import pojahn.game.core.Entity;
import pojahn.lang.Obj;

public class BigParallaxImage extends Entity {

    private Integer minX, maxX, minY, maxY;
    private float rateX, rateY;

    public BigParallaxImage() {
        rateX = rateY = .5f;
    }

    @Override
    public void init() {
        minX = Obj.nonNull(minX, 0);
        minY = Obj.nonNull(minY, 0);
        maxX = Obj.nonNull(maxX, getLevel().getWidth());
        maxY = Obj.nonNull(maxY, getLevel().getHeight());
    }

    public void setMinX(final Integer minX) {
        this.minX = minX;
    }

    public void setMaxX(final Integer maxX) {
        this.maxX = maxX;
    }

    public void setMinY(final Integer minY) {
        this.minY = minY;
    }

    public void setMaxY(final Integer maxY) {
        this.maxY = maxY;
    }

    public void setRateX(final float rateX) {
        this.rateX = rateX;
    }

    public void setRateY(final float rateY) {
        this.rateY = rateY;
    }

    @Override
    public void logistics() {
        final float deltaX = (getEngine().tx() - getEngine().prevTx()) * rateX;
        final float deltaY = (getEngine().ty() - getEngine().prevTy()) * rateY;

        bounds.pos.x += deltaX;
        bounds.pos.y += deltaY;

        bounds.pos.x = Math.max(minX, x());
        bounds.pos.y = Math.max(minY, y());

        bounds.pos.x = Math.min(x(), maxX);
        bounds.pos.y = Math.min(y(), maxY);
    }
}
