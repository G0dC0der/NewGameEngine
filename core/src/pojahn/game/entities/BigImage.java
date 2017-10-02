package pojahn.game.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import pojahn.game.core.Engine;
import pojahn.game.core.Entity;
import pojahn.game.essentials.Animation;
import pojahn.game.essentials.Image2D;

import java.awt.*;

public class BigImage extends Entity {

    public enum RenderStrategy {
        FULL,
        FIXED,
        PORTION,
        REPEAT,
        PARALLAX,
        PARALLAX_REPEAT
    }

    private RenderStrategy strategy;
    private OrthographicCamera parallaxCamera;
    private float ratioX, ratioY, currRotation;
    private boolean initCamera;

    public BigImage() {
        this(RenderStrategy.FULL);
    }

    public BigImage(final RenderStrategy strategy) {
        this.strategy = strategy;
        initCamera = true;
        ratioX = ratioY = .5f;
    }

    public void setRenderStrategy(final RenderStrategy strategy) {
        this.strategy = strategy;
        initCamera = true;
    }

    public RenderStrategy getRenderStrategy() {
        return strategy;
    }

    @Override
    public void setImage(final Animation<Image2D> image) {
        super.setImage(image);
        initCamera = true;
    }

    @Override
    public void render(final SpriteBatch batch) {
        if (getRotation() != 0)
            throw new RuntimeException("A big image can not be rotated.");

        if (initCamera) {
            initCamera = false;
            initParallaxCamera();
        }

        final Engine e = getLevel().getEngine();
        final Dimension screen = e.getScreenSize();
        final Color defColor = batch.getColor();

        switch (strategy) {
            case FULL:
                super.render(batch);
                break;
            case PORTION:
                final float startX = e.tx() - screen.width / 2;
                final float startY = e.ty() - screen.height / 2;
                final float width = screen.width;
                final float height = screen.height;
                final float u = startX / width();
                final float v = startY / height();
                final float u2 = (startX + width) / width();
                final float v2 = (startY + height) / height();
                batch.setColor(tint);
                batch.draw(nextImage(), startX, startY, width, height, u, v, u2, v2);
                batch.setColor(defColor);
                break;
            case FIXED:
                e.hudCamera();
                super.render(batch);
                e.gameCamera();
                break;
            case REPEAT:
                batch.setColor(tint);
                repeat(batch);
                batch.setColor(defColor);
                break;
            case PARALLAX:
                updateParallaxCamera();
                batch.setProjectionMatrix(parallaxCamera.combined);
                super.render(batch);
                e.gameCamera();
                break;
            case PARALLAX_REPEAT:
                batch.setColor(tint);
                updateParallaxCamera();
                batch.setProjectionMatrix(parallaxCamera.combined);
                repeat(batch);
                e.gameCamera();
                batch.setColor(defColor);
                break;
        }
    }

    public float getRatioX() {
        return ratioX;
    }

    public void setRatioX(final float ratioX) {
        this.ratioX = ratioX;
    }

    public float getRatioY() {
        return ratioY;
    }

    public void setRatioY(final float ratioY) {
        this.ratioY = ratioY;
    }

    public void setRatio(final float ratio) {
        ratioX = ratioY = ratio;
    }

    private void initParallaxCamera() {
        if (strategy == RenderStrategy.PARALLAX) {
            parallaxCamera = new OrthographicCamera(width(), height());
            parallaxCamera.setToOrtho(true);
            parallaxCamera.position.set(0, 0, 0);
        } else if (strategy == RenderStrategy.PARALLAX_REPEAT) {
            parallaxCamera = new OrthographicCamera(getLevel().getWidth(), getLevel().getHeight());
            parallaxCamera.setToOrtho(true);
            parallaxCamera.position.set(0, 0, 0);
        }
    }

    private void updateParallaxCamera() {
        final Engine e = getEngine();
        final Dimension screen = e.getScreenSize();
        final float rotation = e.getRotation();

        parallaxCamera.rotate(-currRotation);
        parallaxCamera.rotate(rotation);
        parallaxCamera.zoom = getEngine().getZoom();
        parallaxCamera.position.x = Math.max(screen.width / 2, parallaxCamera.position.x + (e.tx() - e.prevTx()) * ratioX);
        parallaxCamera.position.y = Math.max(screen.height / 2, parallaxCamera.position.y + (e.ty() - e.prevTy()) * ratioY);
        parallaxCamera.update();

        currRotation = rotation;
    }

    private void repeat(final SpriteBatch batch) {
        final int stageWidth = getLevel().getWidth();
        final int stageHeight = getLevel().getHeight();
        int repeatX = (int) (stageWidth / width());
        int repeatY = (int) (stageHeight / height());

        if (stageWidth > repeatX * width())
            repeatX++;
        if (stageHeight > repeatY * height())
            repeatY++;

        for (int x = 0; x < repeatX; x++)
            for (int y = 0; y < repeatY; y++)
                batch.draw(nextImage(), x * width(), y * height());
    }
}
