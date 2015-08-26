package pojahn.game.entities;

import pojahn.game.core.Level.Tile;
import pojahn.game.core.MobileEntity;

public class CrossMovement extends MobileEntity{//TODO: Currently, this one ignores collisions with obstacles.
	
	public enum Movement{
		VERTICAL,
		HORIZONTAL
	}
	
	private Movement movment;
	private boolean leftOrUp;
	
	public CrossMovement(Movement movement){
		if(movement == null)
			throw new IllegalArgumentException("Must set a movement.");
		
		this.movment = movement;
		addTileEvent(tile ->{
			if(tile == Tile.SOLID)
				leftOrUp = !leftOrUp;
		});
	}
	
	public CrossMovement(CrossMovement src){
		this(src.movment);
		copyData(src);
		if(src.cloneEvent != null)
			src.cloneEvent.handleClonded(this);
	}
	
	public void setMovement(Movement movement){
		this.movment = movement;
	}
	
	@Override
	public void logics() {
		if(movment == Movement.HORIZONTAL)
			moveToward(leftOrUp ? 0 : getLevel().getWidth(), y());
		else
			moveToward(x(), leftOrUp ? 0 : getLevel().getHeight());
	}
}
