package pojahn.game.essentials.stages;


import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import pojahn.game.core.Entity;
import pojahn.game.core.Level;
import pojahn.game.essentials.Factory;
import pojahn.game.essentials.Image2D;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public abstract class TileBasedLevel extends Level {

    public static class PositionedCell {
        public Cell cell;
        public int x, y;

        public PositionedCell(Cell cell, int x, int y) {
            this.cell = cell;
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof PositionedCell) {
                PositionedCell pCell = (PositionedCell) obj;
                return this.cell == pCell.cell && (x * 31 + y) == (pCell.x * 31 + pCell.y);
            } else {
                return false;
            }
        }
    }

    private TiledMap map;
    private int tilesX, tilesY, tileWidth, tileHeight;
    private TiledMapTileLayer layer;
    private Map<Integer, PositionedCell> orgTiles;
    private Image2D tileSet;

    protected TileBasedLevel() {
        orgTiles = new HashMap<>();
    }

    public void parse(TiledMap map) throws IOException {
        this.map = map;
        MapProperties props = map.getProperties();
        layer = (TiledMapTileLayer) map.getLayers().get(0);
        tilesX = props.get("width", Integer.class);
        tilesY = props.get("height", Integer.class);
        tileWidth = props.get("tilewidth", Integer.class);
        tileHeight = props.get("tileheight", Integer.class);
        encode();
    }

    @Override
    public int getWidth() {
        return tilesX * tileWidth;
    }

    @Override
    public int getHeight() {
        return tilesY * tileHeight;
    }

    @Override
    protected Tile tileAtInternal(int x, int y) {
        int tileX = x / tileWidth;
        int tileY = y / tileHeight;

        Cell cell = layer.getCell(tileX, tileY);
        if (cell != null) {
            TextureRegion region = cell.getTile().getTextureRegion();
            int regX = region.getRegionX();
            int regY = region.getRegionY();
            int relX = x % tileWidth;
            int relY = y % tileHeight;
            regY -= tileHeight;

            return (tileSet.getPixel(regX + relX, regY + relY) & 0x000000FF) > 0 ? Tile.SOLID : Tile.HOLLOW;
        } else
            return Tile.HOLLOW;
    }

    public void setCell(int tileX, int tileY, Cell cell) {
        if (tileX < 0 || tileX > tilesX || tileY < 0 || tileY > tilesY)
            return;

        Cell org = layer.getCell(tileX, tileY);
        layer.setCell(tileX, tileY, cell);

        int key = tileX * 31 + tileY;
        if (orgTiles.get(key) == null)
            orgTiles.put(key, new PositionedCell(org, tileX, tileY));
    }

    public void transformTiles(int tileCx, int tileCy, int radius, Cell cell) {
        int rr = radius * radius;

        for (int x = tileCx - radius; x <= tileCx + radius; ++x) {
            for (int y = tileCy - radius; y <= tileCy + radius; ++y) {
                int dx = tileCx - x;
                int dy = tileCy - y;
                if ((dx * dx + dy * dy) < rr)
                    setCell(x, y, cell);
            }
        }
    }

    public void restoreTiles() {
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
        return Factory.construct(map);
    }

    @Override
    protected final void clean() {
        super.clean();
        restoreTiles();
    }

    private void encode() throws IOException {
        for (int x = 0; x < tilesX; x++) {
            for (int y = 0; y < tilesY; y++) {
                Cell cell = layer.getCell(x, y);
                if (cell != null) {
                    TextureRegion region = cell.getTile().getTextureRegion();
                    TextureData tdata = region.getTexture().getTextureData();
                    tdata.prepare();
                    Pixmap pix = tdata.consumePixmap();
                    tileSet = new Image2D(pix, true);
                    pix.dispose();
                    return;
                }
            }
        }
    }
}
