package pojahn.game.entities;

import pojahn.game.essentials.stages.TileBasedLevel;

public class Shockwave extends Particle {

    private int tileWidth, tileHeight, size, freq, counter, radius;
    private TileBasedLevel level;
    private boolean first;

    public Shockwave(final int size, final int freq) {
        if (size <= 1)
            throw new IllegalStateException("Size must exceed 1.");

        this.size = size;
        this.freq = freq;

        radius = 1;
        counter = -1;
        first = true;
    }

    @Override
    public Particle getClone() {
        final Shockwave shockwave = new Shockwave(size, freq);
        copyData(shockwave);

        return shockwave;
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
    public void logistics() {
        super.logistics();

        if (first) {
            first = false;

            if (freq <= 0) {
                final int tileX = (int) (x() / tileWidth);
                final int tileY = (int) (y() / tileHeight);
                level.transformTiles(tileX, tileY, size, null);
            } else {
                getLevel().temp(() -> {
                    if (++counter == 0 || counter % freq == 0) {
                        final int tileX = (int) (x() / tileWidth);
                        final int tileY = (int) (y() / tileHeight);

                        level.transformTiles(tileX, tileY, ++radius, null);
                    }
                }, () -> radius > size);
            }
        }
    }
}
