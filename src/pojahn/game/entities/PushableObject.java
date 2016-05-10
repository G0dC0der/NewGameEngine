package pojahn.game.entities;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.Vector2;
import pojahn.game.core.Collisions;
import pojahn.game.core.Entity;
import pojahn.game.core.MobileEntity;

import static pojahn.game.core.Collisions.rectanglesCollide;

public class PushableObject extends MobileEntity { //TODO: This class is oddly written. Test it roughly.

	public float mass, gravity, damping, pushStrength, accX;
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
		accX = 500;
		dummy = new Entity();

		for (MobileEntity mobile : pushers){
			mobile.addObstacle(this);
			addObstacle(mobile);
		}
	}

	@Override
	public void logistics() {
		if (useGravity) {
			if (!canDown()) {
				if (vel.y < 0 && landingSound != null)
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
				
				if(rectanglesCollide(mobile.bounds.toRectangle(), dummy.bounds.toRectangle())){
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

	public void setSlammingSound(Sound sound) {
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

	protected void moveLeft(){
		if(vel.x < tVel.x)
			vel.x += accX * getEngine().delta;
	}

	protected void moveRight(){
		if(-vel.x < tVel.x)
			vel.x -= accX * getEngine().delta;
	}

	protected void drag(){
		float force = mass * gravity;
		vel.y *= 1.0 - (damping * getEngine().delta);

		if(tVel.y < vel.y){
			vel.y += (force / mass) * getEngine().delta;
		}else
			vel.y -= (force / mass) * getEngine().delta;
	}

	protected float getFutureX(){
		return bounds.pos.x - vel.x * getEngine().delta;
	}

	protected float getFutureY(){
		return bounds.pos.y - vel.y * getEngine().delta;
	}

	protected boolean runningLeft(){
		return vel.x > 0;
	}

	protected boolean runningRight(){
		return vel.x < 0;
	}
}