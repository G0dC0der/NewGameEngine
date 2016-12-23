package pojahn.game.essentials;

import com.badlogic.gdx.assets.loaders.resolvers.AbsoluteFileHandleResolver;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapLayers;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import pojahn.game.core.Entity;
import pojahn.game.core.Level.Tile;
import pojahn.game.core.Level.TileLayer;
import pojahn.game.events.Event;
import pojahn.game.events.RenderEvent;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;

import java.util.HashSet;
import java.util.Set;

public class Utils {

    public static Image2D toImage(Color color) {
        return toImage(color, 1 ,1);
    }

    public static Image2D toImage(Color color, int width, int height) {
        Pixmap pixmap = new Pixmap(width, height, Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Image2D image = new Image2D(pixmap);
        pixmap.dispose();

        return image;
    }

    public static void playMusic(Music music, float seconds, float volume) {
        music.setVolume(volume);
        music.play();
        music.setOnCompletionListener(music1 -> {
            music1.play();
            music1.setPosition(seconds);
        });
    }

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

        return array[MathUtils.random(0, array.length - 1)];
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

    public static TiledMap loadTiledMap(FileHandle path){
        String str = path.file().getAbsolutePath();
        TmxMapLoader.Parameters params = new TmxMapLoader.Parameters();
        params.flipY = false;
        TiledMap map = new TmxMapLoader(new AbsoluteFileHandleResolver()).load(str, params);
        MapProperties props = map.getProperties();

        int tilesX = props.get("width", Integer.class);
        int tilesY = props.get("height", Integer.class);

        MapLayers layers = map.getLayers();
        layers.forEach(l->{
            TiledMapTileLayer layer = (TiledMapTileLayer) l;
            Set<TextureRegion> used = new HashSet<>();

            for(int x = 0; x < tilesX; x++){
                for(int y = 0; y < tilesY; y++){
                    TiledMapTileLayer.Cell cell = layer.getCell(x, y);
                    if(cell != null){
                        TextureRegion region = cell.getTile().getTextureRegion();
                        if(used.add(region)){
                            region.flip(false, true);
                        }
                    }
                }
            }
        });

        return map;
    }
}
