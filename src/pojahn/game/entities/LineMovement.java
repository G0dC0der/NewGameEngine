package pojahn.game.entities;

import pojahn.game.core.Level.Tile;
import pojahn.game.core.MobileEntity;

public class LineMovement extends MobileEntity{
	
	public enum Movement{
		VERTICAL,
		HORIZONTAL
	}
	
	private Movement movment;
	private boolean leftOrUp;
	
	public LineMovement(Movement movement){
		if(movement == null)
			throw new IllegalArgumentException("Must set a movement.");
		
		this.movment = movement;
		addTileEvent(tile ->{
			if(tile == Tile.SOLID)
				leftOrUp = !leftOrUp;
		});
	}
	
	@Override
	public LineMovement getClone(){
		LineMovement clone = new LineMovement(movment);
		copyData(clone);
		if(cloneEvent != null)
			cloneEvent.handleClonded(clone);
		
		return clone;
	}
	
	public void setMovement(Movement movement){
		this.movment = movement;
	}
	
	@Override
	public void logics() {
		if(obstacleCollision())
			leftOrUp = !leftOrUp;
		
		if(movment == Movement.HORIZONTAL)
			moveTowards(leftOrUp ? 0 : getLevel().getWidth(), y());
		else
			moveTowards(x(), leftOrUp ? 0 : getLevel().getHeight());
	}
}
