package pojahn.game.entities;

import com.badlogic.gdx.math.Vector2;
import pojahn.game.core.Entity;

public class Missile extends Projectile {
	
	public float thrust, drag, delta;
	private Entity target;
	private Vector2 velocity;
	private Entity[] scanTargets;

	public Missile(float x, float y, Entity... scanTargets) {
		super(x, y, scanTargets);
		velocity = new Vector2();
		this.scanTargets = scanTargets;
		mediumFloaty();
	}
	
	@Override
	public Missile getClone() {
		Missile clone = new Missile(x(), y(), scanTargets);
		copyData(clone);
		if(cloneEvent != null)
			cloneEvent.handleClonded(clone);
		
		return clone;
	}
	
	public void slowAccurate(){
		thrust = 400f;
		drag = 3.5f;
		delta = 1f/50f;
	}
	
	public void mediumFloaty(){
		thrust = 400f;
		drag = 2f;
		delta = 1f/60f;
	}
	
	public void fastVeryFloaty(){
		thrust = 400f;
		drag = 1f;
		delta = 1f/40f;
	}
	
	public void inaccurate(){
		thrust = 400f;
		drag = 0.7f;
		delta = 1f/50f;
	}
	
	public void slowChase(){
		thrust = 400f;
		drag = 10f;
		delta = 1f/60f;
	}

	@Override
	protected void rotate() {
		if(rotates())
			bounds.rotation = (float) Math.toDegrees(Math.atan2(velocity.y, velocity.x));
	}

	@Override
	protected void moveProjectile(Vector2 target) {
		float dx = target.x - x();
		float dy = target.y - y();
		double length = Math.sqrt( dx*dx + dy*dy );
		dx /= length;
		dy /= length;
			 
		float accelx = thrust * dx - drag * velocity.x;
		float accely = thrust * dy - drag * velocity.y;
	 
		velocity.x += delta * accelx;
		velocity.y += delta * accely;
		
		bounds.pos.x += delta * velocity.x;
		bounds.pos.y += delta * velocity.y;
	}

	protected void copyData(Missile clone){
		super.copyData(clone);
		clone.thrust = thrust;
		clone.drag = drag;
		clone.delta = delta;
	}
}
