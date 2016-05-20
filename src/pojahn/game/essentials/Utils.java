package pojahn.game.essentials;

import pojahn.game.core.Entity;
import pojahn.game.core.Level.Tile;
import pojahn.game.core.Level.TileLayer;
import pojahn.game.events.Event;
import pojahn.game.events.RenderEvent;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;

public class Utils {

    public static Entity wrap(RenderEvent renderEvent) {
        return wrap(renderEvent, 0);
    }

    public static Entity wrap(RenderEvent renderEvent, int zIndex) {
        return new Entity() {
            {
                this.zIndex(zIndex);
                this.identifier = "WRAPPER";
            }

            @Override
            public void render(SpriteBatch batch) {
                super.render(batch);
                renderEvent.eventHandling(batch);
            }
        };
    }

    public static Entity wrap(Event event) {
        return new EntityBuilder().events(event).build();
    }

    public static <T> T getRandomElement(T[] array) {
        if (array.length == 0)
            return null;

        return array[MathUtils.random(array.length)];
    }

    public static TileLayer from(Image2D image) {
        TileLayer tileLayer = new TileLayer(image.getWidth(), image.getHeight());
        for (int x = 0; x < tileLayer.width(); x++) {
            for (int y = 0; y < tileLayer.height(); y++) {
                if (!image.isInvisible(x, y)) {
                    tileLayer.setTile(x, y, Tile.SOLID);
                }
            }
        }
        return tileLayer;
    }
}
