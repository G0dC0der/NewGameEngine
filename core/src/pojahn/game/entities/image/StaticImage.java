package pojahn.game.entities.image;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import pojahn.game.core.Entity;

public class StaticImage extends Entity {

    @Override
    public void render(final SpriteBatch batch) {
        getEngine().hudCamera();
        super.render(batch);
        getEngine().gameCamera();
    }
}
