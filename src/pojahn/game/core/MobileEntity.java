package pojahn.game.core;

import com.badlogic.gdx.math.Vector2;
import pojahn.game.core.Level.Tile;
import pojahn.game.essentials.Direction;
import pojahn.game.events.TileEvent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MobileEntity extends Entity {
	
	float prevX, prevY;
	List<TileEvent> tileEvents;
	Direction facing;
	
	private boolean smart;
	private float moveSpeed;
	private boolean frozen, tileEventPause;
	private Set<Entity> obstacles;
	
	public MobileEntity(){
		facing = Direction.E;
		tileEvents = new ArrayList<>();
		obstacles = new HashSet<>();
		moveSpeed = 3;
	}
	
	@Override
	public MobileEntity getClone(){
		MobileEntity clone = new MobileEntity();
		copyData(clone);
		if(cloneEvent != null)
			cloneEvent.handleClonded(clone);
		
		return clone;
	}
	
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

	public void moveTowards(float targetX, float targetY){
		if(smart)
			smartMoveTowards(targetX, targetY, moveSpeed);
		else
			dumbMoveTowards(targetX, targetY, moveSpeed);
	}

	public Direction getFacing() {
		return facing;
	}

	void updateFacing(){
		Direction facing = Collisions.getDirection(this);
		if(facing != null)
			this.facing = facing;
	}
	
	protected void dumbMoveTowards(float targetX, float targetY, float steps){
		Vector2 next = attemptTowards(targetX, targetY, steps);
	    bounds.pos.set(next);
	}
	
	protected void smartMoveTowards(float targetX, float targetY, float steps){
		Vector2 next = attemptTowards(targetX, targetY, steps);
		smartMove(next.x, next.y);
	}
	
	protected boolean smartMove(float targetX, float targetY){
		boolean moved = false;
		
		if(!occupiedAt(targetX, y())){
			bounds.pos.x = targetX;
			moved = true;
		}
		if(!occupiedAt(x(), targetY)){
			bounds.pos.y = targetY;
			moved = true;
		}
		return moved;
	}
	
	protected Vector2 attemptTowards(float targetX, float targetY, float steps){
	    float fX = targetX - x();
	    float fY = targetY - y();
	    float dist = (float)Math.sqrt( fX*fX + fY*fY );
	    float step = steps / dist;

	    return new Vector2(bounds.pos.x + fX * step, bounds.pos.y + fY * step);
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

	public void addTileEvent(TileEvent tileEvent){
		tileEvents.add(tileEvent);
	}
	
	public void addObstacle(Entity obstacle){
		obstacles.add(obstacle);
	}
	
	public void removeObstacle(Entity obstacle){
		obstacles.remove(obstacle);
	}
	
	public void setSmart(boolean smart){
		this.smart = smart;
	}
	
	public boolean isSmart(){
		return smart;
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
	
	public boolean canDown(){
		return !occupiedAt(x(), y()+1);
	}
	
	public boolean canUp(){
		return !occupiedAt(x(), y()-1);
	}
	
	public boolean canLeft(){
		return !occupiedAt(x() - 1, y());
	}
	
	public boolean canRight(){
		return !occupiedAt(x() + 1, y());
	}
	
	public void faceDirection(){
		Direction dir = Collisions.getDirection(x(), y(), prevX, prevY);
		if(dir != null)
			face(dir);
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
		return  l.outOfBounds(x(), y()) ||
                l.outOfBounds(x() + width(), y()) ||
                l.outOfBounds(x() + width(), y() + height()) ||
                l.outOfBounds(x(), y() + height());
	}
	
	public Set<Tile> getOccupyingCells(){
		Level l = getLevel();
		Set<Tile> cells = new HashSet<>();
		
		int x = (int) x();
		int y = (int) y();
		int w = (int) width();
		int h = (int) height();
		
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
			collisionResponse(target);
	}
	
	public void collisionResponse(MobileEntity target) {
		float centerX = x() + width() / 2;
		float centerY = y() + height() / 2;
		
        double overX = ((target.width()  + width() ) /  2.0) - Math.abs((target.x() + target.width()  / 2) - centerX);
        double overY = ((target.height() + height()) /  2.0) - Math.abs((target.y() + target.height() / 2) - centerY);
       
        if(overY > overX){
            if(target.x() > centerX)
            	target.bounds.pos.x += overX;
            else
            	target.bounds.pos.x -= overX;
        } else {
        	if(target.y() > centerY)
        		target.bounds.pos.y += overY;
        	else
        		target.bounds.pos.y -= overY;
        }
	}
	
	protected boolean obstacleCollision(float tempX, float tempY){		
		float realX = x();
		float realY = y();
		move(tempX, tempY);
		boolean col = obstacleCollision();
		move(realX, realY);
		
		return col;
	}
	
	protected boolean obstacleCollision(){		
		for(Entity obstacle : obstacles)
			if(collidesWith(obstacle))
				return true;
		
		return false;
	}
	
	protected void copyData(MobileEntity clone){
		super.copyData(clone);
		clone.moveSpeed = moveSpeed;
		clone.obstacles.addAll(obstacles);
		clone.smart = smart;
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
