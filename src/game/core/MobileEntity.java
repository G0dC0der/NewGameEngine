package game.core;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import game.events.TileEvent;

public class MobileEntity extends Entity{
	
	float prevX, prevY;
	List<TileEvent> tileEvents;
	
	public MobileEntity(){
		tileEvents = new ArrayList<>();
	}
	
	public void logics(){}
	
	public float prevX(){
		return prevX;
	}
	
	public float prevY(){
		return prevY;
	}
	
	public Set<Byte> getOccupyingCells()
	{
		Level l = getLevel();
		Set<Byte> cells = new HashSet<>();
		
		int 	x1  = (int) x() + 1,
				y1  = (int) y() + 1,
				x2 = (int) (x1 + width()  - 1),
				y2 = (int) (y1 + height() - 1);
		
		for(int lx = x1; lx < x2; lx++)
		{
			cells.add(l.get(y1, lx));
			cells.add(l.get(y2, lx));
		}
		for(int ly = y1; ly < y2; ly++)
		{
			cells.add(l.get(ly, x1));
			cells.add(l.get(ly, x2));
		}
		
		return cells;
	}
	
	void runTileEvents(byte tile){
		for(TileEvent tileEvent : tileEvents)
			tileEvent.eventHandling(tile);
	}
}
