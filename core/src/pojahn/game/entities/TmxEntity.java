package pojahn.game.entities;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import pojahn.game.core.MobileEntity;
import pojahn.game.essentials.Animation;
import pojahn.game.essentials.Hitbox;
import pojahn.game.essentials.Image2D;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TmxEntity extends SolidPlatform {

    private final TiledMap tiledMap;
    private final float width, height;
    private TiledMapRenderer tiledMapRenderer;
    private List<TiledMapTileLayer> layers;
    private OrthographicCamera camera;
    private float rotation;

    public TmxEntity(TiledMap tiledMap, MobileEntity... subjects) {
        super(0,0, subjects);
        this.tiledMap = tiledMap;

        layers = new ArrayList<>(tiledMap.getLayers().getCount());
        tiledMap.getLayers().forEach(mapLayer -> layers.add((TiledMapTileLayer)mapLayer));
        Collections.reverse(layers);

        MapProperties props = tiledMap.getProperties();
        int tilesX = props.get("width", Integer.class);
        int tilesY = props.get("height", Integer.class);
        int tileWidth = props.get("tilewidth", Integer.class);
        int tileHeight = props.get("tileheight", Integer.class);

        width = tilesX * tileWidth;
        height = tilesY * tileHeight;
    }

    @Override
    public void init() {
        Dimension screenSize = getEngine().getScreenSize();
        camera = new OrthographicCamera();
        camera.setToOrtho(!getEngine().flippedY(), screenSize.width, screenSize.height);
    }

    @Override
    public void setImage(Animation<Image2D> image) {
        throw new UnsupportedOperationException("");
    }

    @Override
    public void setHitbox(Hitbox hitbox) {
        throw new UnsupportedOperationException("");
    }

    @Override
    public void render(SpriteBatch batch) {
        if (tiledMapRenderer == null)
            tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap, batch);

        updateDimensions();
        camera.position.set(getEngine().tx() - x(), getEngine().ty() - y(), 0);
        camera.rotate(-rotation);
        rotation = getEngine().getRotation() + getRotation();
        camera.rotate(rotation);
        camera.zoom = getEngine().getZoom();

        //Should support rotation and flip
        camera.update();

        com.badlogic.gdx.graphics.Color color = batch.getColor();
        batch.setColor(tint);

        tiledMapRenderer.setView(camera);
        layers.forEach(tiledMapRenderer::renderTileLayer);


        batch.setColor(color);
    }

    private void updateDimensions() {
        bounds.size.width = width;
        bounds.size.height = height;
    }
}
