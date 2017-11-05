package pojahn.game.entities.image;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import pojahn.game.core.Engine;
import pojahn.game.core.Entity;
import pojahn.game.essentials.Animation;
import pojahn.game.essentials.Image2D;

import java.awt.*;

public class ParallaxImage extends Entity {

    private float rateX, rateY;
    private OrthographicCamera camera;

    public ParallaxImage() {
        rateX = rateY = .5f;
    }

    @Override
    public void init() {
        camera = new OrthographicCamera();
        camera.setToOrtho(true);
    }

    public void setRateX(final float rateX) {
        this.rateX = rateX;
    }

    public void setRateY(final float rateY) {
        this.rateY = rateY;
    }

    @Override
    public void render(final SpriteBatch batch) {
        final Engine e = getEngine();
        final Dimension screen = e.getScreenSize();
        final float minX = screen.width / 2;
        final float minY = screen.height / 2;

        camera.position.x = MathUtils.clamp(camera.position.x + (e.tx() - e.prevTx()) * rateX, minX, width() - minX);
        camera.position.y = MathUtils.clamp(camera.position.y + (e.ty() - e.prevTy()) * rateY, minY, height() - minY);
        camera.zoom = getEngine().getZoom();
        camera.update();

        batch.setProjectionMatrix(camera.combined);

        super.render(batch);

        e.gameCamera();
    }
}