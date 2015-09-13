package pojahn.game.entities;

import pojahn.game.core.Level;
import pojahn.game.core.Level.Tile;
import pojahn.game.core.MobileEntity;

public class TransformablePlatform extends SolidPlatform{

	private Tile tile;
	
	public TransformablePlatform(float x, float y, MobileEntity... subjects) {
		super(x, y, subjects);
		tile = Tile.SOLID;
	}
	
	@Override
	public TransformablePlatform getClone() {
		TransformablePlatform clone = new TransformablePlatform(x(),y(),subjects.toArray(new MobileEntity[subjects.size()]));
		copyData(clone);
		if(cloneEvent != null)
			cloneEvent.handleClonded(clone);
		
		return clone;
	}

	@Override
	public void logics() {
		super.logics();
		deform(prevX(), prevY(), null);
		
		if(x() != prevX() || y() != prevY()){
			deform(x(), y(), tile);
		}
	}
	
	public void setTile(Tile tile){
		this.tile = tile;
	}
	
	protected void copyData(TransformablePlatform clone){
		super.copyData(clone);
		clone.tile = tile;
	}
	
	private void deform(float x, float y, Tile tile) {
		Level l = getLevel();

		float width = width();
		float height = height();
		float halfHeight = halfHeight();

		for (int x2 = 0; x2 < width; x2++) {
			for (int y2 = 0; y2 < halfHeight; y2++) {
				int posX = (int) (x2 + x);
				int topY = (int) (y2 + y);
				int bottomY = (int) (height - y2);
				
				l.setTileOnLayer(posX, topY, tile);
				l.setTileOnLayer(posX, bottomY, tile);
			}
		}
	}
}
