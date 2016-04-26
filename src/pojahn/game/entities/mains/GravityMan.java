package pojahn.game.entities.mains;

import pojahn.game.core.Level;
import pojahn.game.core.PlayableEntity;
import pojahn.game.essentials.geom.EarthBound;
import pojahn.game.essentials.Keystrokes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.Vector2;

public class GravityMan extends PlayableEntity implements EarthBound{

	public Vector2 vel, tVel, slidingTVel;
	public float accX, mass, gravity, damping, wallGravity, wallDamping, jumpStrength, wallJumpHorizontalStrength;
	private int jumpButtonPressedCounter;
	private boolean isWallSliding, allowWallSlide, allowWallJump;
	private Sound jumpSound;
	
	public GravityMan(){
		vel = new Vector2();
		tVel = new Vector2(230, -800);
		slidingTVel = new Vector2(0, -100);
		
		accX = 500;
		
		mass = 1.0f;
		gravity = -500;
		damping = 0.0001f;
		jumpStrength = 180;
		
		wallGravity = -90;
		wallDamping = 1.1f;
		wallJumpHorizontalStrength = 120;
		
		allowWallSlide = allowWallJump = true;
	}
	
	public GravityMan getClone(){
		GravityMan clone = new GravityMan();
		copyData(clone);
		if(cloneEvent != null)
			cloneEvent.handleClonded(clone);
		
		return clone;
	}
	
	@Override
	public void logics() {
		run();
		wallSlide();
		jump();
	}
	
	public void setJumpSound(Sound sound){
		this.jumpSound = sound;
	}
	
	protected void wallSlide(){
		Keystrokes strokes = getKeysDown();
		isWallSliding = isWallSliding() && canDown();
		
		if(allowWallJump && isWallSliding && strokes.jump){
			if(canRight())
				vel.x = -wallJumpHorizontalStrength;
			else if(canLeft())
				vel.x = wallJumpHorizontalStrength;
			
			playSound();
			vel.y = jumpStrength * 1.5f;
		} else if(allowWallJump && isWallSliding){
			if(strokes.left && !canRight())
				isWallSliding = false;
			else if(strokes.right && !canLeft())
				isWallSliding = false;
		}
	}
	
	protected void jump(){
		Keystrokes strokes = getKeysDown();
		boolean jump = !isFrozen() && strokes.jump && vel.y == 0 && !canDown();

		if(jumpButtonPressedCounter > 0){
			if(Gdx.input.isKeyPressed(getController().jump))
				jumpButtonPressedCounter++;
			else
				jumpButtonPressedCounter = 0;
		}
		
		if(jump){
			vel.y = jumpStrength;
			jumpButtonPressedCounter = 1;
			
			if(strokes.left && !partialLeft())
				vel.x = -wallJumpHorizontalStrength;
			else if(strokes.right  && !partialRight())
				vel.x = wallJumpHorizontalStrength;
		}
		
		if(jumpButtonPressedCounter == 5)
			vel.y = jumpStrength * 1.5f;
		if(jump && jumpButtonPressedCounter == 1)
			playSound();
		
		drag();
		
		float futureY = getFutureY();
		if(!occupiedAt(x(), futureY))
			applyYForces();
		else{
			if(vel.y < 0)
				tryDown(10);
			
			vel.y = 0;
		}
	}
	
	protected void run(){
		Keystrokes strokes = getKeysDown();
		boolean left = !isFrozen() && strokes.left;
		boolean right = !isFrozen() && strokes.right;
		boolean moving = left || right;
		
		if(left){
			moveLeft();
		}else if(right){
			moveRight();
		}
		
		if(!moving){
			if(vel.x > 0){
				moveRight();
				if(vel.x < 0)
					vel.x = 0;
			}
			else if(vel.x < 0){
				moveLeft();
				if(vel.x > 0)
					vel.x = 0;
			}
		}
		
		if(vel.x != 0){
			float futureX = getFutureX();
			
			if(vel.x > 0)
				runLeft(futureX);
			else if(vel.x < 0)
				runRight(futureX);
		}
	}
	
	protected void runLeft(float targetX){
		for(float next = bounds.pos.x; next >= targetX; next-= 0.5f) {
			if(!occupiedAt(next, y())){
				bounds.pos.x = next;
				if(!occupiedAt(x(), bounds.pos.y + 1) && occupiedAt(x(), bounds.pos.y + 2))
					bounds.pos.y++;
			} else if(canSlopeLeft(next)) {
				move(next, bounds.pos.y - 1);
				tryDown(1);
			} else {
				vel.x = 0;
				return;
			}
		}
	}
	
	protected void runRight(float targetX){
		for (float next = x(); next <= targetX; next += 0.5f) {
			if (!occupiedAt(next, y())) {
				bounds.pos.x = next;
				if (!occupiedAt(x(), y() + 1) && occupiedAt(x(), y() + 2))
					bounds.pos.y++;
			} else if (canSlopeRight(next)) {
				move(next, y() - 1);
				tryDown(1);
			} else {
				vel.x = 0;
				break;
			}
		}
	}
	
	protected boolean canSlopeLeft(float targetX){
		int y = (int)bounds.pos.y - 1, tar = (int) targetX;
		Level l = getLevel();
		
		for (int i = 0; i < height(); i++)
			if(l.isSolid(tar, y + i))
				return false;

		return !obstacleCollision(targetX, bounds.pos.y);
	}
	
	protected boolean canSlopeRight(float targetX)
	{
		int y = (int)y() - 1, tar = (int) (targetX + width());
		Level l = getLevel();
		
		for (int i = 0; i < height(); i++)
			if(l.isSolid(tar, y + i))
				return false;
		
		return !obstacleCollision(targetX, y());
	}
	
	protected boolean isWallSliding(){
		Keystrokes strokes = getKeysDown();

		if(!allowWallSlide || strokes.down)
			return false;

		return
			 ((strokes.left  || isWallSliding) && !canLeft()) ||
			 ((strokes.right || isWallSliding) && !canRight());
	}
	
	protected boolean partialLeft(){
		Level l = getLevel();
		int x = (int)x() - 1;
		int partialHeight = (int) (height() / 3);
		
		for(int y = (int)y(); y < y() + partialHeight; y++)
			if(l.isSolid(x, y))
				return false;
		
//		for(int y = (int)(y() + height()) - partialHeight; y < y() + height(); y++)
//			if(l.isSolid(x, y))
//				return false;
		
		return !obstacleCollision(x() - 1, y());
	}
	
	protected boolean partialRight(){
		Level l = getLevel();
		int x = (int) (x() + width() + 1);
		int partialHeight = (int) (height() / 3);
		
		for(int y = (int)y(); y < y() + partialHeight; y++)
			if(l.isSolid(x, y))
				return false;
		
//		for(int y = (int)(y() + height()) - partialHeight; y < y() + height(); y++)
//			if(l.isSolid(x, y))
//				return false;
		
		return !obstacleCollision(x() - 1, y());
	}

	@Override
	public float vx() {
		return vel.x;
	}

	@Override
	public float vy() {
		return vel.y;
	}

	@Override
	public void setVx(float vx) {
		vel.x = vx;
	}

	@Override
	public void setVy(float vy) {
		vel.y = vy;
	}

	@Override
	public float thermVx() {
		return isWallSliding && allowWallSlide ? slidingTVel.x :tVel.x;
	}

	@Override
	public float thermVy() {
		return isWallSliding && allowWallSlide ? slidingTVel.y :tVel.y;
	}

	@Override
	public void setThermVx(float thermVx) {
		tVel.x = thermVx;
	}

	@Override
	public void setThermVy(float thermVy) {
		tVel.y = thermVy;
	}

	@Override
	public float getAccelerationX() {
		return accX;
	}

	@Override
	public float getGravity() {
		return isWallSliding && allowWallSlide ? wallGravity : gravity;
	}

	@Override
	public float getMass() {
		return mass;
	}

	@Override
	public float getDamping() {
		return isWallSliding && allowWallSlide ? wallDamping : damping;
	}

	@Override
	public float getDelta() {
		return getEngine().delta;
	}
	
	protected void copyData(GravityMan clone){
		super.copyData(clone);
		clone.vel.set(vel);
		clone.tVel.set(tVel);
		clone.slidingTVel.set(slidingTVel);
		clone.allowWallJump = allowWallJump;
		clone.allowWallSlide = allowWallSlide;
		clone.accX = accX;
		clone.mass = mass;
		clone.gravity = gravity;
		clone.damping = damping;
		clone.wallGravity = wallGravity;
		clone.wallDamping = wallDamping;
		clone.jumpStrength = jumpStrength;
		clone.wallJumpHorizontalStrength = wallJumpHorizontalStrength;
		clone.jumpSound = jumpSound;
	}
	
	private void playSound(){
		if(jumpSound != null)
			jumpSound.play(sounds.calc());
	}
}
