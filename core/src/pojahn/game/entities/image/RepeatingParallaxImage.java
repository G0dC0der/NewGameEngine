package pojahn.game.entities.image;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import pojahn.game.core.Engine;
import pojahn.game.core.Entity;
import pojahn.game.essentials.Image2D;
import pojahn.game.essentials.geom.Dimension;
import pojahn.lang.Obj;

public class RepeatingParallaxImage extends Entity {

    private OrthographicCamera camera;
    private Integer repeatX, repeatY;
    private float rateX, rateY;

    public RepeatingParallaxImage() {
        rateX = rateY = .5f;
    }

    public void setRepeatX(final int repeatX) {
        this.repeatX = repeatX;
    }

    public void setRepeatY(final int repeatY) {
        this.repeatY = repeatY;
    }

    public void setRateX(final float rateX) {
        this.rateX = rateX;
    }

    public void setRateY(final float rateY) {
        this.rateY = rateY;
    }

    @Override
    public void init() {
        repeatX = Obj.nonNull(repeatX, (getLevel().getWidth() / (int)width()) + 1);
        repeatY = Obj.nonNull(repeatY, (getLevel().getHeight() / (int)height()) + 1);

        camera = new OrthographicCamera();
        camera.setToOrtho(true);
    }

    @Override
    public void render(final SpriteBatch batch) {
        final Engine e = getEngine();
        final Dimension screen = e.getScreenSize();
        final float minX = screen.width / 2;
        final float minY = screen.height / 2;

        camera.position.x = MathUtils.clamp(camera.position.x + (e.tx() - e.prevTx()) * rateX, minX, (width() * repeatX) - minX);
        camera.position.y = MathUtils.clamp(camera.position.y + (e.ty() - e.prevTy()) * rateY, minY, (height() * repeatY) - minY);
        camera.zoom = getEngine().getZoom();
        camera.update();

        batch.setProjectionMatrix(camera.combined);

        final Image2D image = nextImage();

        for (int x = 0; x < repeatX; x++)
            for (int y = 0; y < repeatY; y++)
                super.basicRender(batch, image, x() + (x * width()), y() + (y * height()));

        e.gameCamera();
    }
}
