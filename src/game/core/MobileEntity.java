package game.core;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import game.core.Level.Tile;
import game.essentials.Direction;
import game.events.TileEvent;

public class MobileEntity extends Entity{
	
	float prevX, prevY;
	List<TileEvent> tileEvents;
	
	private float moveSpeed;
	private boolean frozen, tileEventPause;
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
	
	public void freeze(){
		frozen = true;
	}
	
	public void unfreeze(){
		frozen = false;
	}
	
	public boolean isFrozen(){
		return frozen;
	}

	public void moveToward(float targetX, float targetY){
		moveToward(targetX, targetY, moveSpeed);
	}
	
	public void moveToward(float targetX, float targetY, float steps){
	    float fX = targetX - x();
	    float fY = targetY - y();
	    double dist = Math.sqrt( fX*fX + fY*fY );
	    double step = steps / dist;

	    bounds.pos.x += fX * step;
	    bounds.pos.y += fY * step;
	}

	public void stepBack(){
		move(prevX,prevY);
	}
	
	public void forgetPast(){
		prevX = bounds.pos.x;
		prevY = bounds.pos.y;
	}
	
	public float prevX(){
		return prevX;
	}
	
	public float prevY(){
		return prevY;
	}
	
	public void pauseTileEvents(boolean pause){
		this.tileEventPause = pause;
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
		for(int i = steps; i > 0; i--){
			if(!occupiedAt(x() - i, y())){
				move(x() - i, y());
				return true;
			}
		}
		return false;
	}
	
	public boolean tryRight(int steps){
		for(int i = steps; i > 0; i--){
			if(!occupiedAt(x() + i, y())){
				move(x() + i, y());
				return true;
			}
		}
		return false;
	}
	
	public boolean tryUp(int steps){
		for(int i = steps; i > 0; i--){
			if(!occupiedAt(x(), y() - i)){
				move(x(), y() - i);
				return true;
			}
		}
		return false;
	}
	
	public boolean tryDown(int steps){
		for(int i = steps; i > 0; i--){
			if(!occupiedAt(x(), y() + i)){
				move(x(), y() + i);
				return true;
			}
		}
		return false;
	}
	
	public void face(Direction dir){
		switch(dir){
			case N:
				setRotation(-90);
				flipY = false;
				flipX = false;
				break;
			case NE:
				setRotation(-45);
				flipX = flipY = false;
				break;
			case E:
				setRotation(0);
				flipX = flipY = false;
				break;
			case SE:
				setRotation(45);
				flipX = flipY = false;
				break;
			case S:
				setRotation(90);
				flipY = false;
				flipX = false;
				break;
			case SW:
				setRotation(90 + 45);
				flipX = false;
				flipY = true;
				break;
			case W:
				setRotation(0);
				flipX = true;
				flipY = false;
				break;
			case NW:
				setRotation(45);
				flipX = true;
				flipY = false;
				break;
		}
	}
	
	public boolean occupiedAt(float targetX, float targetY){
		float 	realX = x(),
				realY = y();
		move(targetX, targetY);
		
		boolean occupied = getOccupyingCells().contains(Tile.SOLID);
		if(occupied){
			move(realX,realY);
			return true;
		}
		occupied = obstacleCollision();
		move(realX,realY);
		return occupied;
	}
	
	public boolean outOfBounds(){
		Level l = getLevel();
		return l.outOfBounds(x(), y()) || l.outOfBounds(x() + width(), y()) || l.outOfBounds(x() + width(), y() + height()) || l.outOfBounds(x(), y() + height());
	}
	
	public Set<Tile> getOccupyingCells(){
		Level l = getLevel();
		Set<Tile> cells = new HashSet<>();
		
		int x = (int) x() + 1;
		int y = (int) y() + 1;
		int w = (int) width() - 1;
		int h = (int) height() - 1;
		
		for(int xcord = x; xcord < x + w; xcord++){
			cells.add(l.tileAt(xcord, y));
			cells.add(l.tileAt(xcord, y + h));
		}
		for(int ycord = y; ycord < y + h; ycord++){
			cells.add(l.tileAt(x, ycord));
			cells.add(l.tileAt(x + w, ycord));
		}
		
		return cells;
	}
	
	public void adjust(MobileEntity target, boolean harsh){
		float nextX = target.x() + x() - prevX;
		float nextY = target.y() + y() - prevY;
		
		if(target.occupiedAt(nextX, nextY))
			target.move(nextX, nextY);
		
		if(harsh)
			collisionRespone(target);
	}
	
	public void collisionRespone(MobileEntity target){
//		if(collidesWithMultiple(target) == null)
//			return;
		
		float centerX = x() + width() / 2;
		float centerY = y() + height() / 2;
		
        double overX = ((target.width()  + width() ) /  2.0) - Math.abs((target.x() + target.width()  / 2) - centerX);
        double overY = ((target.height() + height()) /  2.0) - Math.abs((target.y() + target.height() / 2) - centerY);
       
        if(overY > overX){
            if(target.x() > centerX)
            	target.bounds.pos.x += overX;
            else
            	target.bounds.pos.x -= overX;
        } else{
        	if(target.y() > centerY)
        		target.bounds.pos.y += overY;
        	else
        		target.bounds.pos.y -= overY;
        }
	}
	
	protected boolean obstacleCollision(){		
		for(Entity obstacle : obstacles)
			if(obstacle.isPresent() && collidesWith(obstacle))
				return true;
		
		return false;
	}
	
	void runTileEvents(Tile tile){
		if(!tileEventPause){
			for(TileEvent tileEvent : tileEvents){
				tileEvent.eventHandling(tile);
			}
		}
	}
	
	void setPrevs(){
		prevX = bounds.pos.x;
		prevY = bounds.pos.y;
	}
}
