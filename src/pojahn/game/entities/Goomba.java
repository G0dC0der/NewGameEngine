package pojahn.game.entities;

import com.badlogic.gdx.audio.Sound;

import pojahn.game.core.Collisions;
import pojahn.game.core.MobileEntity;
import pojahn.game.essentials.Direction;

public class Goomba extends PathDrone {
	
	private MobileEntity[] enemies;
	private Particle deathImg;
	private int hitFrames, hitSubjects[], playDelay, playSoundCounter;
	private Direction hitbox;
	private Sound walkingSound, hitSound;

	public Goomba(float x, float y, Direction hitbox, MobileEntity... enemies) {
		super(x, y);

		if (isValidDirection(hitbox))
			this.hitbox = hitbox;
		else
			throw new IllegalArgumentException("Illegal direction: " + hitbox);

		this.enemies = enemies;
		hitSubjects = new int[enemies.length];
		hitFrames = 100;
	}

	public void setWalkingSound(Sound sound, int playDelay) {
		walkingSound = sound;
		this.playDelay = playDelay;
	}

	public void setDeathSound(Sound sound) {
		hitSound = sound;
	}

	@Override
	public void logics() {
		super.logics();

		playWalkingSound();

		for (int i = 0; i < enemies.length; i++) {
			MobileEntity mobile = enemies[i];
			hitSubjects[i]--;

			if (collidesWith(mobile)) {
				if (hitSubjects[i] <= 0 && isAttacking(mobile)) {
					getLevel().discard(this);
					if (deathImg != null)
						getLevel().add(deathImg.getClone().move(x(), x()));

					if (walkingSound != null)
						walkingSound.stop();
					if (hitSound != null)
						hitSound.play(sounds.calc());
				} else {
					mobile.runActionEvent(this);
					if (hitFrames > 0)
						hitSubjects[i] = hitFrames;
				}
			}
		}
	}

	public void setDeathParticle(Particle deathImg) {
		this.deathImg = deathImg;
	}

	public void subjectHitFrames(int frames) {
		hitFrames = frames;
	}

	private boolean isValidDirection(Direction dir) {
		return dir == Direction.N || dir == Direction.E || dir == Direction.S || dir == Direction.W;
	}

	private boolean isAttacking(MobileEntity mobile) {
		Direction f = Collisions.getDirection(mobile.x(), mobile.y(), mobile.prevX(), mobile.prevY());

		switch (hitbox) {
		case N:
			return f == Direction.S || f == Direction.SW || f == Direction.SE;
		case E:
			return f == Direction.W || f == Direction.SW || f == Direction.NW;
		case S:
			return f == Direction.N || f == Direction.NW || f == Direction.NE;
		case W:
			return f == Direction.E || f == Direction.SE || f == Direction.NE;
		default:
			throw new RuntimeException();
		}
	}

	private void playWalkingSound() {
		if (walkingSound != null && ++playSoundCounter % playDelay == 0)
			walkingSound.play(sounds.calc());
	}
}