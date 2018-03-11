package pojahn.game.entities;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import pojahn.game.core.Entity;
import pojahn.game.essentials.Animation;
import pojahn.game.essentials.Hitbox;
import pojahn.game.essentials.Image2D;
import pojahn.game.essentials.geom.Dimension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TmxEntity extends Entity {

    private final TiledMap tiledMap;
    private final float width, height;
    private TiledMapRenderer tiledMapRenderer;
    private List<TiledMapTileLayer> layers;
    private OrthographicCamera camera;
    private float rotation;

    public TmxEntity(final TiledMap tiledMap) {
        this.tiledMap = tiledMap;

        layers = new ArrayList<>(tiledMap.getLayers().getCount());
        tiledMap.getLayers().forEach(mapLayer -> layers.add((TiledMapTileLayer) mapLayer));
        Collections.reverse(layers);

        final MapProperties props = tiledMap.getProperties();
        final int tilesX = props.get("width", Integer.class);
        final int tilesY = props.get("height", Integer.class);
        final int tileWidth = props.get("tilewidth", Integer.class);
        final int tileHeight = props.get("tileheight", Integer.class);

        width = tilesX * tileWidth;
        height = tilesY * tileHeight;

        updateDimensions();
    }

    @Override
    public void init() {
        final Dimension screenSize = getEngine().getScreenSize();
        camera = new OrthographicCamera();
        camera.setToOrtho(!getEngine().flippedY(), screenSize.width, screenSize.height);
    }

    @Override
    public void setImage(final Animation<Image2D> image) {
        throw new UnsupportedOperationException("");
    }

    @Override
    public void setHitbox(final Hitbox hitbox) {
        throw new UnsupportedOperationException("");
    }

    @Override
    public void render(final SpriteBatch batch) {
        if (tiledMapRenderer == null) {
            tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap, batch);
        }

        updateDimensions();
        camera.position.set(getEngine().tx() - x(), getEngine().ty() - y(), 0);
        camera.rotate(-rotation);
        rotation = getEngine().getRotation() + getRotation();
        camera.rotate(rotation);
        camera.zoom = getEngine().getZoom();

        //Should support rotation and flip
        camera.update();

        tiledMapRenderer.setView(camera);
        layers.forEach(tiledMapRenderer::renderTileLayer);
    }

    private void updateDimensions() {
        bounds.size.width = width;
        bounds.size.height = height;
    }
}
