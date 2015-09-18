package pojahn.game.entities;

import com.badlogic.gdx.audio.Sound;

import pojahn.game.core.MobileEntity;
import pojahn.game.essentials.Animation;
import pojahn.game.essentials.Direction;
import pojahn.game.essentials.Image2D;

public class Boo extends MobileEntity {

	private MobileEntity victim;
	private Animation<Image2D> hideImage, huntImage;
	private Direction victimFacing;
	private boolean stone, resetHideImage, resetHuntImage;
	private boolean hunting, allowSound;
	private Sound detectSound;
	public float velocity, acc, maxSpeed, unfreezeRadius;

	public Boo(float x, float y, MobileEntity victim) {
		super();
		move(x, y);
		this.victim = victim;
		maxSpeed = 3;
		acc = 0.03f;
		unfreezeRadius = 70;
		resetHideImage = resetHuntImage = true;
		allowSound = true;
	}
	
	//TODO: Get clone

	@Override
	public void logics() {
		float dir = victim.x() - victim.prevX();
		if (dir > 0)
			victimFacing = Direction.W;
		else if (dir < 0)
			victimFacing = Direction.E;

		flipX = victim.centerX() < x() + width();

		if (canSneak()) {
			if (maxSpeed > velocity + acc)
				velocity += acc;

			if (resetHideImage && hideImage != null)
				hideImage.reset();

			setMoveSpeed(velocity);
			moveTowards(victim.centerX(), victim.centerY());

			if (!hunting)
				setImage(huntImage);

			if (stone && !hunting)
				victim.addObstacle(this); //TODO: Is this executed every frames?
			
			if(detectSound != null && allowSound)
				detectSound.play(sounds.calc());
			
			allowSound = false;
			hunting = true;
		} else {
			if (hideImage != null && hunting)
				super.setImage(hideImage);

			velocity = 0;

			if (resetHuntImage)
				huntImage.reset();

			if (stone) {
				if (hunting)
					victim.removeObstacle(this); //TODO: Also check here

				hunting = false;
				return;// Avoid doing a collision check.
			}
			hunting = false;
			allowSound = true;
		}
		if (collidesWith(victim)) {
			victim.runActionEvent(this);
			velocity = 0;
		}
	}

	@Override
	public void setImage(Animation<Image2D> obj) {
		super.setImage(obj);
		huntImage = obj;
	}

	public void setHuntImage(Animation<Image2D> huntImage) {
		setImage(huntImage);
	}

	public void setHideImage(Animation<Image2D> hideImage) {
		this.hideImage = hideImage;
	}

	public void solidify(boolean stone) {
		this.stone = stone;
	}

	public void setDetectSound(Sound sound) {
		detectSound = sound;
	}

	public void resetHideImage(boolean reset) {
		this.resetHideImage = reset;
	}

	public void resetHuntImage(boolean reset) {
		this.resetHuntImage = reset;
	}

	protected boolean canSneak() {
		if (stone && !hunting && dist(victim) < unfreezeRadius)
			return false;

		boolean toTheLeft = centerX() > victim.x();

		if (toTheLeft && (victimFacing == Direction.NW || victimFacing == Direction.W || victimFacing == Direction.SW))
			return true;
		if (!toTheLeft && (victimFacing == Direction.NE || victimFacing == Direction.E || victimFacing == Direction.SE))
			return true;

		return false;
	}
}
