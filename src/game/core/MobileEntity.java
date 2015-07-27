package game.core;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import game.core.Level.Tile;
import game.events.TileEvent;

public class MobileEntity extends Entity{
	
	float prevX, prevY;
	List<TileEvent> tileEvents;
	
	private float moveSpeed;
	private Set<Entity> obstacles;
	
	public MobileEntity(){
		tileEvents = new ArrayList<>();
		obstacles = new HashSet<>();
		moveSpeed = 3;
	}
	
	public void logics(){}
	
	public float getMoveSpeed() {
		return moveSpeed;
	}

	public void setMoveSpeed(float moveSpeed) {
		this.moveSpeed = moveSpeed;
	}

	public void moveToward(float targetX, float targetY){
		moveToward(targetX, targetY, moveSpeed);
	}
	
	public void moveToward(float targetX, float targetY, float steps){
	    float fX = targetX - x();
	    float fY = targetY - y();
	    double dist = Math.sqrt( fX*fX + fY*fY );
	    double step = steps / dist;

	    bounds.x += fX * step;
	    bounds.y += fY * step;
	}

	public void stepBack(){
		move(prevX,prevY);
	}
	
	public float prevX(){
		return prevX;
	}
	
	public float prevY(){
		return prevY;
	}
	
	public void addTileEvent(TileEvent tileEvent){
		tileEvents.add(tileEvent);
	}
	
	public void addObstacle(Entity obstacle){
		obstacles.add(obstacle);
	}
	
	public void removeObstacle(Entity obstacle){
		obstacles.remove(obstacle);
	}
	
	public boolean tryLeft(int steps){
		for(int i = steps; i == 0; i--){
			if(!occupiedAt(x() - i, y())){
				move(x() - i, y());
				return true;
			}
		}
		return false;
	}
	
	public boolean tryRight(int steps){
		for(int i = steps; i == 0; i--){
			if(!occupiedAt(x() + i, y())){
				move(x() - i, y());
				return true;
			}
		}
		return false;
	}
	
	public boolean tryUp(int steps){
		for(int i = steps; i == 0; i--){
			if(!occupiedAt(x(), y() - i)){
				move(x(), y() - i);
				return true;
			}
		}
		return false;
	}
	
	public boolean tryDown(int steps){
		for(int i = steps; i == 0; i--){
			if(!occupiedAt(x(), y() + i)){
				move(x(), y() + i);
				return true;
			}
		}
		return false;
	}
	
	public boolean occupiedAt(float targetX, float targetY){
		float 	realX = x(),
				realY = y();
		move(targetX, targetY);
		
		boolean occupied = !getOccupyingCells().contains(Tile.SOLID) && !obstacleCollision();
		move(realX,realY);
		return occupied;
	}
	
	public boolean outOfBounds(){
		return outOfBounds(x(), y());
	}
	
	public boolean outOfBounds(float targetX, float targetY){
		Level l = getLevel();

		if(targetX >= l.getWidth() 	||
		   targetY >= l.getHeight() || 
		   targetX < 0 ||
		   targetY < 0)
			return true;
		
		return false;
	}
	
	public Set<Tile> getOccupyingCells()
	{
		Level l = getLevel();
		Set<Tile> cells = new HashSet<>();
		
		int 	x1  = (int) x() + 1,
				y1  = (int) y() + 1,
				x2 = (int) (x1 + width()  - 1),
				y2 = (int) (y1 + height() - 1);
		
		for(int lx = x1; lx < x2; lx++){
			cells.add(l.tileAt(y1, lx));
			cells.add(l.tileAt(y2, lx));
		}
		for(int ly = y1; ly < y2; ly++){
			cells.add(l.tileAt(ly, x1));
			cells.add(l.tileAt(ly, x2));
		}
		
		return cells;
	}
	
	public void adjust(MobileEntity target, boolean harsh)
	{
		float nextX = target.x() + x() - prevX;
		float nextY = target.y() + y() - prevY;
		
		if(target.occupiedAt(nextX, nextY))
			target.move(nextX, nextY);
		
		if(harsh)
			collisionRespone(target);
	}
	
	public void collisionRespone(MobileEntity target)
	{
//		if(collidesWithMultiple(target) == null)
//			return;
		
		float centerX = x() + width() / 2;
		float centerY = y() + height() / 2;
		
        double overX = ((target.width()  + width() ) /  2.0) - Math.abs((target.x() + target.width()  / 2) - centerX);
        double overY = ((target.height() + height()) /  2.0) - Math.abs((target.y() + target.height() / 2) - centerY);
       
        if(overY > overX)
        {
            if(target.x() > centerX)
            	target.bounds.x += overX;
            else
            	target.bounds.x -= overX;
        }
        else
        {
        	if(target.y() > centerY)
        		target.bounds.y += overY;
        	else
        		target.bounds.y -= overY;
        }
	}
	
	protected boolean obstacleCollision(){
		return obstacleCollision(x(), y());
	}
	
	protected boolean obstacleCollision(float x, float y){
		float realX = x();
		float realY = y();
		move(x,y);
		boolean collides = false;
		
		for(Entity e : obstacles)
			if(collidesWith(e)){
				collides = true;
				break;
			}
		
		move(realX, realY);
		return collides;
	}
	
	void runTileEvents(Tile tile){
		for(TileEvent tileEvent : tileEvents)
			tileEvent.eventHandling(tile);
	}
}
