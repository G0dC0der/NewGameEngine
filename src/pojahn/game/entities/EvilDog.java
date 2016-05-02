package pojahn.game.entities;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.Vector2;

import pojahn.game.core.Collisions;
import pojahn.game.core.Entity;
import pojahn.game.core.MobileEntity;
import pojahn.game.essentials.Animation;
import pojahn.game.essentials.Image2D;

public class EvilDog extends MobileEntity {

	public float thrust, drag, delta, vx, vy;
	private float maxDistance;
	private boolean hunting;
	private int soundDelay, soundCounter;
	private Entity[] targets;
	private Particle impact;
	private Animation<Image2D> idleImg, huntImg;
	private Sound hitSound;

	public EvilDog(float x, float y, float maxDistance, Entity... targets) {
		move(x, y);
		this.maxDistance = maxDistance;
		this.targets = targets;
		thrust = 500f;
		drag = .5f;
		delta = 1f / 60f;
	}

	public void collisionAnim(Particle impact) {
		this.impact = impact;
	}

	public void setCollisionSound(Sound sound) {
		hitSound = sound;
	}
	
	public void setCollisionSoundDelay(int delay){
		soundDelay = delay;
	}

	public boolean hunting() {
		return hunting;
	}

	public void idleImage(Animation<Image2D> idleImg) {
		this.idleImg = idleImg;
	}

	@Override
	public void setImage(Animation<Image2D> obj) {
		huntImg = obj;
		super.setImage(obj);
	}

	@Override
	public void logistics() {
		++soundCounter;
		
		if (isFrozen())
			return;

		Entity closest = Collisions.findClosest(this, targets);
		if (closest != null && (maxDistance < 0 || maxDistance > Collisions.distance(this, closest))) {
			setImage(huntImg);
			hunting = true;

			Vector2 norP = Collisions.normalize(closest, this);

			float accelx = thrust * norP.x - drag * vx;
			float accely = thrust * norP.y - drag * vy;

			vx += delta * accelx;
			vy += delta * accely;

			bounds.pos.x += delta * vx;
			bounds.pos.y += delta * vy;

			if (collidesWith(closest)) {
				if (impact != null)
					getLevel().add(impact.getClone().move(x(), y()));

				closest.runActionEvent(this);
				
				if(hitSound != null && soundCounter > soundDelay){
					hitSound.play(sounds.calc());
					soundCounter = 0;
				}
			}
		} else if (idleImg != null) {
			hunting = false;
			setImage(idleImg);
		}
	}
}
