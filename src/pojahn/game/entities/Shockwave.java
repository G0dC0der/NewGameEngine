package pojahn.game.entities;

import pojahn.game.essentials.stages.TileBasedLevel;

public class Shockwave extends Particle{

	private int tileWidth, tileHeight, size, freq, counter, radius;
	private TileBasedLevel level;
	
	public Shockwave(int size, int freq){
		this.size = size;
		this.freq = freq;
	}
	
	@Override
	public void init() {
		if(!(getLevel() instanceof TileBasedLevel))
			throw new IllegalStateException("Can only use Shockwave for a tile based map(TileBsedLevel).");
		
		level = (TileBasedLevel) getLevel();
		tileWidth = level.getTileWidth();
		tileHeight = level.getTileHeight();
	}
	
	@Override
	public void logistics() {
		super.logistics();
		
		if((counter == 0 || ++counter % freq == 0) && radius <= size) {
			int tileX = (int) (x() / tileWidth);
			int tileY = (int) (y() / tileHeight);
			
			level.transformTiles(tileX, tileY, ++radius, null);
		}
	}
}
