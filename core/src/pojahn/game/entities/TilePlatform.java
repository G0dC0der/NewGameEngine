package pojahn.game.entities;

import pojahn.game.core.Level;
import pojahn.game.core.Level.Tile;
import pojahn.game.core.MobileEntity;
import pojahn.game.essentials.Utils;

public class TilePlatform extends SolidPlatform {

    private Tile tile;
    private Level.TileLayer tileLayer;
    private MobileEntity[] subjects;

    public TilePlatform(final float x, final float y, final MobileEntity... subjects) {
        super(x, y, subjects);
        tile = Tile.SOLID;
        this.subjects = subjects;
    }

    @Override
    public TilePlatform getClone() {
        final TilePlatform clone = new TilePlatform(x(), y(), subjects);
        copyData(clone);
        if (cloneEvent != null)
            cloneEvent.handleClonded(clone);

        return clone;
    }

    @Override
    public void init() {
        super.init();

        if (tileLayer == null) {
            initTileLayer();
        }

        getLevel().addTileLayer(tileLayer);
    }

    @Override
    public void logistics() {
        super.logistics();
        tileLayer.setPosition((int) x() + 2, (int) y() + 2);
    }

    @Override
    public void dispose() {
        super.dispose();
        getLevel().removeTileLayer(tileLayer);
    }

    public void setTile(final Tile tile) {
        this.tile = tile;
    }

    private void initTileLayer() {
        tileLayer = Utils.rectangularLayer((int) width() - 4, (int) height() - 4, tile);
    }

    protected void copyData(final TilePlatform clone) {
        super.copyData(clone);
        clone.tile = tile;
        if (tileLayer == null)
            initTileLayer();
        clone.tileLayer = tileLayer.copy();
    }
}
