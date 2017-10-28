package pojahn.game.entities;

import pojahn.game.essentials.stages.TileBasedLevel;

public class Shockwave extends Particle {

    private final int size, freq;
    private int tileWidth, tileHeight, counter, radius;
    private TileBasedLevel level;

    public Shockwave(final int size, final int freq) {
        if (size <= 1)
            throw new IllegalStateException("Size must exceed 1.");

        this.size = size;
        this.freq = freq;

        radius = 1;
        counter = -1;
    }

    @Override
    public Shockwave getClone() {
        final Shockwave clone = new Shockwave(size, freq);
        copyData(clone);
        if (cloneEvent != null) {
            cloneEvent.handleClonded(clone);
        }

        return clone;
    }

    @Override
    public void init() {
        if (!(getLevel() instanceof TileBasedLevel))
            throw new IllegalStateException("Can only use Shockwave for a tile based map(TileBasedLevel).");

        level = (TileBasedLevel) getLevel();
        tileWidth = level.getTileWidth();
        tileHeight = level.getTileHeight();
    }

    @Override
    protected boolean completed() {
        return !isVisible() || getImage().hasEnded();
    }

    @Override
    protected void step() {
        if (freq <= 0) {
            final int tileX = (int) (x() / tileWidth);
            final int tileY = (int) (y() / tileHeight);
            level.transformTiles(tileX, tileY, size, null);
        } else if (size > radius && (++counter == 0 || counter % freq == 0)) {
            final int tileX = (int) (x() / tileWidth);
            final int tileY = (int) (y() / tileHeight);

            level.transformTiles(tileX, tileY, ++radius, null);
        }
    }
}