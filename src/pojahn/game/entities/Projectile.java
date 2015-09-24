package pojahn.game.entities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.badlogic.gdx.math.Vector2;

import pojahn.game.core.Collisions;
import pojahn.game.core.Entity;
import pojahn.game.core.Level;
import pojahn.game.core.Level.Tile;
import pojahn.game.core.MobileEntity;
import pojahn.lang.ArrayUtils;

public abstract class Projectile extends MobileEntity{

	protected Particle impact, gunfire, trailer;
	protected Entity scanTargets[], targets[];
	protected Vector2 manualTarget;
	protected boolean rotate;
	protected int trailerDelay;

	private List<Entity> allTargets;
	private int trailerCounter;
	private boolean fired;
	
	public Projectile(float x, float y, Entity[] scanTargets){
		move(x,y);
		rotate = true;
		setScanTargets(scanTargets);
	}
	
	@Override
	public Projectile getClone() {
		throw new UnsupportedOperationException("Can not get a clone of an abstract class.");
	}
	
	public void setTargets(Entity... targets){
		this.targets = targets;
		createCachedList();
	}
	
	public void setScanTargets(Entity... scanTargets){
		this.scanTargets = scanTargets;
		createCachedList();
	}
	
	public void setImpact(Particle impact) {
		this.impact = impact;
	}

	public void setGunfire(Particle gunfire) {
		this.gunfire = gunfire;
	}

	public void setTrailer(Particle trailer) {
		this.trailer = trailer;
	}

	public void setTrailerDelay(int trailerDelay) {
		this.trailerDelay = trailerDelay;
	}
	
	public void rotate(boolean rotate){
		this.rotate = rotate;
	}

	public void setManualTarget(float targetX, float targetY){
		this.manualTarget = new Vector2(targetX, targetY);
	}
	
	public void setManualTarget(Vector2 manualTarget){
		this.manualTarget = manualTarget;
	}

	@Override
	public final void logics() {
		if(manualTarget != null){
			fire();
		}
		
		if(!fired){
			Entity target = Collisions.findClosestSeeable(this, scanTargets);
			
			if(target != null){
				fire();
				targetDetected(target);
			}
		}
		
		if(fired){
			move(manualTarget == null ? getTarget() : manualTarget);
			collisionCheck();
			
			if(trailer != null && ++trailerCounter % trailerDelay == 0){
				Vector2 rare = getRarePosition();
				getLevel().add(trailer.getClone().move(rare.x - trailer.halfWidth(), rare.y - trailer.halfHeight()));
			}
		}
	}
	
	protected abstract void move(Vector2 target);
	
	protected abstract void targetDetected(Entity target);
	
	protected abstract Vector2 getTarget();
	
	protected void rotate(){
		if(rotate){
			Vector2 target = getTarget();
			bounds.rotation = (float) Collisions.getAngle(centerX(), centerY(), target.x, target.y);
		}
	}
	
	protected void fire(){
		fired = true;
		if(gunfire != null)
			getLevel().add(gunfire.getClone().center(this));
	}
	
	protected void collisionCheck(){
		Level l = getLevel();
		
		if (l.tileAt( getFrontPosition()) == Tile.SOLID || l.tileAt(getRarePosition()) == Tile.SOLID)
			impact(null);

		for(Entity target : allTargets){
			if(collidesWith(target)){
				impact(target);
				break;
			}
		}
	}
	
	protected void impact(Entity victim){
		if(victim != null && victim.hasActionEvent())
			victim.runActionEvent(this);

		Vector2 front = getFrontPosition();
		Level l = getLevel();
		
		if(impact != null)
			l.add(impact.getClone().move(front.x - impact.halfWidth(), front.y - impact.halfHeight()));
		
		l.discard(this);
	}
	
	protected void copyData(Projectile clone){
		super.copyData(clone);
		clone.impact = impact;
		clone.gunfire = gunfire;
		clone.trailer = trailer;
		clone.trailerDelay = trailerDelay;
		clone.rotate = rotate;
		clone.scanTargets = ArrayUtils.refCopy(scanTargets, Entity.class);
		clone.targets = ArrayUtils.refCopy(targets, Entity.class);
		clone.allTargets = new ArrayList<>(allTargets);
	}
	
	private void createCachedList(){
		allTargets = new ArrayList<>();
		allTargets.addAll(Arrays.asList(scanTargets));
		allTargets.addAll(Arrays.asList(targets));
	}
}