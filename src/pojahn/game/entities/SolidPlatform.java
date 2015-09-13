package pojahn.game.entities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import pojahn.game.core.Collisions;
import pojahn.game.core.MobileEntity;
import pojahn.game.essentials.Hitbox;

public class SolidPlatform extends PathDrone{
	
	public enum FollowMode{
		NONE,
		NORMAL,
		STRICT
	}

	protected List<MobileEntity> subjects;
	private List<MobileEntity> interactingSujects;
	private FollowMode followMode;
	private float scanSize;
	
	public SolidPlatform(float x, float y, MobileEntity... subjects) {
		super(x, y);
		interactingSujects = new ArrayList<>(subjects.length);
		setSubjects(subjects);
		setFollowMode(FollowMode.NORMAL);
	}
	
	public SolidPlatform getClone() {
		SolidPlatform clone = new SolidPlatform(x(), y(), subjects.toArray(new MobileEntity[subjects.size()]));
		copyData(clone);
		if(cloneEvent != null)
			cloneEvent.handleClonded(clone);
		
		return clone;
	}
	
	@Override
	public void logics() {
		super.logics();
		interactingSujects.clear();

		float x = x() - scanSize;
		float y = y() - scanSize;
		float w = width() + scanSize * 2;
		float h = height() + scanSize * 2;
		
		for(MobileEntity sub : subjects){
			if(Collisions.rectanglesCollide(x, y, w, h, sub.x(), sub.y(), sub.width(), sub.height())){
				interactingSujects.add(sub);
				
				float nextX = sub.x() + (x() - prevX());
				float nextY = sub.y() + (y() - prevY());
				
				if(!sub.occupiedAt(nextX, nextY))
					sub.move(nextX, nextY);
				
				if(Collisions.rectanglesCollide(bounds.toRectangle(), sub.bounds.toRectangle()))
					collisionRespone(sub);
			}
		}
	}
	
	public void setFollowMode(FollowMode followMode){
		this.followMode = followMode;
		switch (followMode) {
			case NONE:
				scanSize = 0;
				break;
			case NORMAL:
				scanSize = 1;
				break;
			case STRICT:
				scanSize = getMoveSpeed() + 1;
				break;
		}
	}
	
	public List<MobileEntity> getInteractingSubjects(){
		return new ArrayList<>(interactingSujects);
	}
	
	@Override
	public void setMoveSpeed(float moveSpeed) {
		super.setMoveSpeed(moveSpeed);
		setFollowMode(this.followMode);
	}
	
	public void setSubjects(MobileEntity... subjects){
		dispose();
		this.subjects = Arrays.asList(subjects);
		this.subjects.forEach(sub-> sub.addObstacle(this));
	}
	
	@Override
	@Deprecated
	public void setHitbox(Hitbox hitbox) {
		throw new UnsupportedOperationException("SolidPlatforms are restricted to rectangular hitbox.");
	}
	
	@Override
	public void dispose() {
		if(subjects != null)
			subjects.forEach(sub-> sub.removeObstacle(this));
	}
	
	protected void copyData(SolidPlatform clone){
		super.copyData(clone);
		
		clone.subjects.addAll(subjects);
		clone.followMode = followMode;
		clone.scanSize = scanSize;
	}
}