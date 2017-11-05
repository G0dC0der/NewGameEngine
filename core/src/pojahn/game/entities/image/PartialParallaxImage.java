package pojahn.game.entities.image;

import com.badlogic.gdx.math.MathUtils;
import pojahn.game.core.Engine;
import pojahn.game.core.Entity;
import pojahn.lang.Obj;

public class PartialParallaxImage extends Entity {

    private Float minX, maxX, minY, maxY;
    private float rateX, rateY;

    public PartialParallaxImage() {
        rateX = rateY = .5f;
    }

    public void setMinX(final Float minX) {
        this.minX = minX;
    }

    public void setMaxX(final Float maxX) {
        this.maxX = maxX;
    }

    public void setMinY(final Float minY) {
        this.minY = minY;
    }

    public void setMaxY(final Float maxY) {
        this.maxY = maxY;
    }

    public void setRateX(final float rateX) {
        this.rateX = rateX;
    }

    public void setRateY(final float rateY) {
        this.rateY = rateY;
    }

    @Override
    public void init() {
        minX = Obj.nonNull(minX, 0f);
        minY = Obj.nonNull(minY, 0f);

        maxX = Obj.nonNull(maxX, width() - getEngine().getScreenSize().width / 2);
        maxY = Obj.nonNull(maxY, height() - getEngine().getScreenSize().height / 2);
    }

    @Override
    public void logistics() {
        final Engine e = getEngine();

        bounds.pos.x = MathUtils.clamp(x() + (e.tx() - e.prevTx()) * rateX, minX, maxX);
        bounds.pos.y = MathUtils.clamp(y() + (e.ty() - e.prevTy()) * rateY, minY, maxY);
    }
}
