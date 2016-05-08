package pojahn.game.entities;

import com.badlogic.gdx.math.Vector2;

import pojahn.game.core.Entity;

public class Bullet extends Projectile {
	
	private Vector2 target;
	private Entity[] scanTargets;

	public Bullet(float x, float y, Entity... scanTargets) {
		super(x, y, scanTargets);
        this.scanTargets = scanTargets;
	}
	
	@Override
	public Bullet getClone() {
		Bullet clone = new Bullet(x(),y(),scanTargets);
		copyData(clone);
		if(cloneEvent != null)
			cloneEvent.handleClonded(clone);
		
		return clone;
	}

	@Override
	protected void moveProjectile(Vector2 target) {
		moveTowards(target.x, target.y);
	}
}