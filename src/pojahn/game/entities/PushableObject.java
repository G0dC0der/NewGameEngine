package pojahn.game.entities;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.Vector2;

import pojahn.game.core.Collisions;
import pojahn.game.core.Entity;
import pojahn.game.core.MobileEntity;
import pojahn.game.essentials.geom.EarthBound;

public class PushableObject extends MobileEntity implements EarthBound {

	public float mass, gravity, damping, pushStrength;
	public Vector2 vel, tVel;
	private boolean useGravity, mustStand;
	private MobileEntity[] pushers;
	private Entity dummy;
	private int pushingSoundDelay, pushingSoundCounter;
	private Sound landingSound, pushingSound;

	public PushableObject(float x, float y, MobileEntity... pushers) {
		vel = new Vector2();
		tVel = new Vector2(230, -1200);
		move(x, y);
		this.pushers = pushers;
		useGravity = true;
		mass = 1.0f;
		gravity = -500;
		damping = 0.0001f;
		pushStrength = 500;
		dummy = new Entity();

		for (MobileEntity mobile : pushers){
			mobile.addObstacle(this);
			addObstacle(mobile);
		}
	}

	@Override
	public void logics() {
		if (useGravity) {
			if (!canDown()) {
				if (vel.y < -100 && landingSound != null)
					landingSound.play(sounds.calc());
				vel.y = 0;
			} else {
				drag();
				float nextY = getFutureY();

				if (!occupiedAt(x(), nextY))
					move(x(), nextY);
				else
					tryDown(10);
			}
		}

		boolean moved = false;
		if (pushStrength > 0.0f) {
			for (MobileEntity mobile : pushers) {
				dummy.move(x() - 1, y());
				dummy.bounds.size.width = width() + 2;
				
				if(mustStand && mobile.canDown())
					continue;
				
				if(Collisions.rectanglesCollide(mobile.bounds.toRectangle(), dummy.bounds.toRectangle())){
					if(Collisions.leftMost(mobile, this) == mobile){
						moveRight();
						moved = true;
						float nextX = getFutureX();
						if(!occupiedAt(nextX, y()))
							move(nextX, y());
						else
							tryRight(10);
					} else if (Collisions.rightMost(mobile, this) == mobile){
						moveLeft();
						moved = true;
						float nextX = getFutureX();
						if(!occupiedAt(nextX, y()))
							move(nextX, y());
						else
							tryLeft(10);
					}
				}
			}
		}
		
		if(!moved){
			if(runningRight()){
				moveLeft();
				if(runningLeft())
					vel.x = 0;
			}
			else if(runningLeft()){
				moveRight();
				if(runningRight())
					vel.x = 0;
			}
			
			float nextX = getFutureX();
			if(!occupiedAt(nextX, y()))
				move(nextX,y());
		}

		if (x() != prevX() && pushingSound != null && ++pushingSoundCounter % pushingSoundDelay == 0)
			pushingSound.play(sounds.calc());
	}

	public void useGravity(boolean gravity) {
		useGravity = gravity;
	}

	public void mustStand(boolean mustStand) {
		this.mustStand = mustStand;
	}

	public void setSlamingSound(Sound sound) {
		landingSound = sound;
	}

	public void setPushingSound(Sound sound, int delay) {
		pushingSound = sound;
		pushingSoundDelay = delay;
	}

	@Override
	public void dispose() {
		for (MobileEntity mobile : pushers)
			mobile.removeObstacle(this);
	}

	@Override
	public Vector2 getVelocity() {
		return vel;
	}

	@Override
	public Vector2 getThermalVelocity() {
		return tVel;
	}

	@Override
	public Vector2 getPosition() {
		return bounds.pos;
	}

	@Override
	public float getAccelerationX() {
		return pushStrength;
	}

	@Override
	public float getGravity() {
		return gravity;
	}

	@Override
	public float getMass() {
		return mass;
	}

	@Override
	public float getDamping() {
		return damping;
	}

	@Override
	public float getDelta() {
		return getEngine().delta;
	}
}