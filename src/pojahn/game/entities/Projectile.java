package pojahn.game.entities;

import com.badlogic.gdx.math.Vector2;
import pojahn.game.core.Collisions;
import pojahn.game.core.Entity;
import pojahn.game.core.Level;
import pojahn.game.core.Level.Tile;
import pojahn.game.core.MobileEntity;

import java.util.Arrays;
import java.util.stream.Stream;

public abstract class Projectile extends MobileEntity {

	private Particle impact, gunfire, trailer;
	private Entity scanTargets[], otherTargets[], target;
	private Vector2 manualTarget;
	private boolean rotate;
	private int trailerDelay;

	private int trailerCounter;
	private boolean fired;
	
	public Projectile(float x, float y, Entity... scanTargets){
		move(x,y);
		rotate = true;
		this.scanTargets = scanTargets;
	}

	@Override
	public Projectile getClone() {
		throw new NullPointerException("Can not get a clone of an abstract class.");
	}

	public void setOtherTargets(Entity... targets){
		this.otherTargets = targets;
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

	public boolean rotates() {
		return rotate;
	}

	public void setManualTarget(float targetX, float targetY){
		this.manualTarget = new Vector2(targetX, targetY);
	}
	
	public void setManualTarget(Vector2 manualTarget){
		this.manualTarget = manualTarget;
	}

	public Vector2 getTarget() {
		if(manualTarget != null)
			return manualTarget;
		else {
			return target == null ? null : new Vector2(target.x(), target.y());
		}
	}

	@Override
	public final void logistics() {
		if(!fired) {
			if(manualTarget != null) {
				fire();
			} else {
				target = Collisions.findClosestSeeable(this, scanTargets);
				if(target != null){
					fire();
				}
			}
		}

		if(fired) {
			moveProjectile(getTarget());
			collisionCheck();
			rotate();

			if(trailer != null && ++trailerCounter % trailerDelay == 0){
				Vector2 rare = getRarePosition();
				getLevel().add(trailer.getClone().move(rare.x - trailer.halfWidth(), rare.y - trailer.halfHeight()));
			}
		}
	}

	protected abstract void moveProjectile(Vector2 target);

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
		
		if (l.tileAt( getFrontPosition()) == Tile.SOLID || l.tileAt(getRarePosition()) == Tile.SOLID) {
			impact(null);
		} else {
			Stream.concat(Arrays.asList(otherTargets).stream(), Arrays.asList(scanTargets).stream())
				  .filter(this::collidesWith)
				  .findFirst()
				  .ifPresent(this::impact);
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
		clone.scanTargets =  scanTargets;
		clone.otherTargets = otherTargets;
	}
}