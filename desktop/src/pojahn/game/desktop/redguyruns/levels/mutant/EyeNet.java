package pojahn.game.desktop.redguyruns.levels.mutant;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import pojahn.game.core.Entity;
import pojahn.game.essentials.Animation;
import pojahn.game.essentials.Image2D;

public class EyeNet extends Entity {

    private int count;
    private float padding;
    private Animation<Image2D> rope;
    private Animation<Image2D> connector1;
    private Animation<Image2D> connector2;
    private OrthographicCamera camera;

    public EyeNet() {
    }

    public void setCount(final int count) {
        this.count = count;
    }

    public void setPadding(final float padding) {
        this.padding = padding;
    }

    public void setRope(final Animation<Image2D> rope) {
        this.rope = rope;
    }

    public void setConnector1(final Animation<Image2D> connector1) {
        this.connector1 = connector1;
    }

    public void setConnector2(final Animation<Image2D> connector2) {
        this.connector2 = connector2;
    }

    @Override
    public void init() {
        camera = new OrthographicCamera();
        camera.setToOrtho(true, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    @Override
    public void render(final SpriteBatch batch) {
        rotateWorld(batch);

        final Image2D ropeImg = rope.getObject();
        final Image2D con1Img = connector1.getObject();
        final Image2D con2Img = connector2.getObject();

        for (int i = 0; i < count; i++) {
            final Image2D con = i % 2 == 0 ? con1Img : con2Img;

            final float x = x() + ((padding + ropeImg.getWidth()) * i);
            final float y = y();

            batch.draw(ropeImg, x, y);
            batch.draw(con, x - (con.getWidth() / 2), y + (ropeImg.getWidth() / 2) - (con.getHeight() / 2) - 1);
        }

        restore();
    }

    private void restore() {
        getEngine().gameCamera();

        camera.rotateAround(new Vector3(x(), y(), 0), getEngine().getGameCamera().direction, -getRotation());
        camera.update();
    }

    private void rotateWorld(final SpriteBatch batch) {
        camera.position.set(getEngine().tx(), getEngine().ty(), 0);
        camera.rotateAround(new Vector3(x(), y(), 0), getEngine().getGameCamera().direction, getRotation());
        camera.update();

        batch.setProjectionMatrix(camera.combined);
    }
}