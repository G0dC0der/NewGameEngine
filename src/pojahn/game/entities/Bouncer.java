package pojahn.game.entities;

import com.badlogic.gdx.audio.Sound;

import pojahn.game.core.Collisions;
import pojahn.game.core.MobileEntity;
import pojahn.game.entities.mains.GravityMan;
import pojahn.game.essentials.Direction;

public class Bouncer extends MobileEntity { //TODO: Change targets to EarthBound?

	private GravityMan[] targets;
	private Sound bounceSound;
	private Direction dir;
	private float power;

	public Bouncer(float x, float y, Direction dir, GravityMan... targets) {
		move(x, y);
		this.dir = dir;
		this.targets = targets;
		power = 150;
	}

	public void setPower(float power) {
		this.power = power;
	}

	@Override
	public void logistics() {
		for (GravityMan man : targets) {
			if (collidesWith(man)) {
				Direction dir = this.dir == null ? Collisions.getDirection(x(), y(), man.x(), man.y()) : this.dir;
				switch (dir) {
					case N:
						man.vel.y = power;
						break;
					case NE:
						man.vel.y = power;
						man.vel.x = -power;
						break;
					case E:
						man.vel.x = -power;
						break;
					case SE:
						man.vel.y = -power;
						man.vel.x = -power;
						break;
					case S:
						man.vel.y = -power;
						break;
					case SW:
						man.vel.y = -power;
						man.vel.x = power;
						break;
					case W:
						man.vel.x = power;
						break;
					case NW:
						man.vel.y = power;
						man.vel.x = power;
						break;
				}
				if (bounceSound != null)
					bounceSound.play(sounds.calc());
			}
		}
	}
}