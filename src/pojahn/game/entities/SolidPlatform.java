package pojahn.game.entities;

import java.util.ArrayList;
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

	private MobileEntity[] subjects;
	private List<MobileEntity> intersectors;
	private FollowMode followMode;
	private float scanSize;
	
	public SolidPlatform(float x, float y, MobileEntity... subjects) {
		super(x, y);
		intersectors = new ArrayList<>(subjects.length);
		this.subjects = subjects;
		setFollowMode(FollowMode.NORMAL);
	}

    @Override
	public SolidPlatform getClone() {
		SolidPlatform clone = new SolidPlatform(x(), y(), subjects);
		copyData(clone);
		if(cloneEvent != null)
			cloneEvent.handleClonded(clone);
		
		return clone;
	}
	
	@Override
	public void logistics() {
		super.logistics();
		intersectors.clear();

		float x = x() - scanSize;
		float y = y() - scanSize;
		float w = width() + scanSize * 2;
		float h = height() + scanSize * 2;
		
		for(MobileEntity sub : subjects){
			if(Collisions.rectanglesCollide(x, y, w, h, sub.x(), sub.y(), sub.width(), sub.height())){
				intersectors.add(sub);
				
				float nextX = sub.x() + (x() - prevX());
				float nextY = sub.y() + (y() - prevY());
				
				if(!sub.occupiedAt(nextX, nextY))
					sub.move(nextX, nextY);
				
				if(Collisions.rectanglesCollide(bounds.toRectangle(), sub.bounds.toRectangle()))
					collisionResponse(sub);
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
	
	public List<MobileEntity> getActiveSubjects(){
		return new ArrayList<>(intersectors);
	}
	
	@Override
	public void setMoveSpeed(float moveSpeed) {
		super.setMoveSpeed(moveSpeed);
		setFollowMode(this.followMode);
	}

	@Override
	@Deprecated
	public void setHitbox(Hitbox hitbox) {
		throw new UnsupportedOperationException("SolidPlatforms are restricted to rectangular hitbox.");
	}
	
	@Override
	public void dispose() {
		if(subjects != null) {
			for(MobileEntity sub : subjects) {
				sub.removeObstacle(this);
			}
		}
	}

	protected void copyData(SolidPlatform clone){
		super.copyData(clone);

		clone.subjects = subjects;
		clone.followMode = followMode;
		clone.scanSize = scanSize;
	}
}