package pojahn.game.entities;

import pojahn.game.core.Level;
import pojahn.game.core.Level.Tile;
import pojahn.game.core.MobileEntity;

import java.awt.*;

public class TransformablePlatform extends SolidPlatform {

    private Tile tile;
    private Level.TileLayer tileLayer;
    private MobileEntity[] subjects;
    private boolean prepared;

    public TransformablePlatform(float x, float y, MobileEntity... subjects) {
        super(x, y, subjects);
        tile = Tile.SOLID;
        this.subjects = subjects;
    }

    @Override
    public TransformablePlatform getClone() {
        TransformablePlatform clone = new TransformablePlatform(x(), y(), subjects);
        copyData(clone);
        if (cloneEvent != null)
            cloneEvent.handleClonded(clone);

        return clone;
    }

    @Override
    public void logistics() {
        super.logistics();

        if(!prepared) {
            prepare();
        }

        tileLayer.setPosition((int)x() + 2, (int)y() + 2);
    }

    @Override
    public void dispose() {
        super.dispose();
        getLevel().removeTileLayer(tileLayer);
    }

    private void prepare() {
        prepared = true;
        tileLayer = new Level.TileLayer((int)width() - 4, (int)height() - 4);
        tileLayer.fill(tile);

        getLevel().addTileLayer(tileLayer);
    }

    public void setTile(Tile tile) {
        this.tile = tile;
    }

    protected void copyData(TransformablePlatform clone) {
        super.copyData(clone);
        clone.tile = tile;
    }
}
