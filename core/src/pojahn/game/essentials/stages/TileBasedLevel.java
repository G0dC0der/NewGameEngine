package pojahn.game.essentials.stages;


import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import pojahn.game.core.BaseLogic;
import pojahn.game.core.Entity;
import pojahn.game.core.Level;
import pojahn.game.entities.TmxEntity;
import pojahn.game.essentials.Image2D;
import pojahn.game.essentials.geom.Size;
import pojahn.game.events.Event;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public abstract class TileBasedLevel extends Level {

    private TiledMap map;
    private int tilesX, tilesY, tileWidth, tileHeight;
    private TiledMapTileLayer layer;
    private Map<Integer, PositionedCell> orgTiles;
    private Image2D tileSet;

    protected TileBasedLevel() {
        orgTiles = new HashMap<>();
    }

    public void parse(final TiledMap map) throws IOException {
        this.map = map;
        final MapProperties props = map.getProperties();
        layer = (TiledMapTileLayer) map.getLayers().get(0);
        tilesX = props.get("width", Integer.class);
        tilesY = props.get("height", Integer.class);
        tileWidth = props.get("tilewidth", Integer.class);
        tileHeight = props.get("tileheight", Integer.class);
        encode();
    }

    //TODO
    //public abstract TiledMap getMap(); Try to remove parse method

    @Override
    public int getWidth() {
        return tilesX * tileWidth;
    }

    @Override
    public int getHeight() {
        return tilesY * tileHeight;
    }

    @Override
    protected Tile tileAtInternal(final int x, final int y) {
        final int tileX = x / tileWidth;
        final int tileY = y / tileHeight;

        final Cell cell = layer.getCell(tileX, tileY);
        if (cell != null) {
            final TextureRegion region = cell.getTile().getTextureRegion();
            final int regX = region.getRegionX();
            int regY = region.getRegionY();
            final int relX = x % tileWidth;
            final int relY = y % tileHeight;
            regY -= tileHeight;

            return (tileSet.getPixel(regX + relX, regY + relY) & 0x000000FF) > 0 ? Tile.SOLID : Tile.HOLLOW;
        } else
            return Tile.HOLLOW;
    }

    public void setCell(final int tileX, final int tileY, final Cell cell) {
        if (tileX < 0 || tileX > tilesX || tileY < 0 || tileY > tilesY)
            return;

        final Cell org = layer.getCell(tileX, tileY);
        layer.setCell(tileX, tileY, cell);

        final int key = tileX * 31 + tileY;
        if (orgTiles.get(key) == null)
            orgTiles.put(key, new PositionedCell(org, tileX, tileY));
    }

    public void transformTiles(final int tileCx, final int tileCy, final int radius, final Cell cell) {
        final int rr = radius * radius;

        for (int x = tileCx - radius; x <= tileCx + radius; ++x) {
            for (int y = tileCy - radius; y <= tileCy + radius; ++y) {
                final int dx = tileCx - x;
                final int dy = tileCy - y;
                if ((dx * dx + dy * dy) < rr)
                    setCell(x, y, cell);
            }
        }
    }

    private void restoreTiles() {
        orgTiles.values().forEach(holder -> {
            layer.setCell(holder.x, holder.y, holder.cell);
        });
        orgTiles.clear();
    }

    public int getTileWidth() {
        return tileWidth;
    }

    public int getTileHeight() {
        return tileHeight;
    }

    public Entity getWorldImage() {
        return new TmxEntity(map);
    }

    public Rectangle getRectangle(final int tileX, final int tileY, final int tilesX, final int tilesY) {
        return new Rectangle(tileX * getTileWidth(), tileY * getTileHeight(), tilesX * getTileWidth(), tilesY * getTileHeight());
    }

    public Vector2 center(final int tileX, final int tileY, final Size entitySize) {
        final float x = (tileX * getTileWidth()) + (getTileWidth() / 2) - (entitySize.width / 2);
        final float y = (tileY * getTileHeight()) + (getTileHeight() / 2) - (entitySize.height / 2);

        return new Vector2(x, y);
    }

    public void runOnceWhenMainCollides(final Event event, final int tileX, final int tileY, final int tilesX, final int tilesY) {
        runOnceWhen(event, () -> getAliveMainCharacters()
                .stream()
                .map(main -> BaseLogic.rectanglesCollide(main.bounds.toRectangle(), getRectangle(tileX, tileY, tilesX, tilesY)))
                .findFirst()
                .orElse(false));
    }

    @Override
    protected final void clean() {
        super.clean();
        restoreTiles();
    }

    private void encode() throws IOException {
        for (int x = 0; x < tilesX; x++) {
            for (int y = 0; y < tilesY; y++) {
                final Cell cell = layer.getCell(x, y);
                if (cell != null) {
                    final TextureRegion region = cell.getTile().getTextureRegion();
                    final TextureData tdata = region.getTexture().getTextureData();
                    tdata.prepare();
                    final Pixmap pix = tdata.consumePixmap();
                    tileSet = new Image2D(pix, true);
                    pix.dispose();
                    return;
                }
            }
        }
    }

    private static class PositionedCell {
        Cell cell;
        int x, y;

        PositionedCell(final Cell cell, final int x, final int y) {
            this.cell = cell;
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj instanceof PositionedCell) {
                final PositionedCell pCell = (PositionedCell) obj;
                return this.cell == pCell.cell && (x * 31 + y) == (pCell.x * 31 + pCell.y);
            } else {
                return false;
            }
        }
    }
}
