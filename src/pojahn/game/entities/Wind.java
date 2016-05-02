package pojahn.game.entities;

import pojahn.game.core.MobileEntity;
import pojahn.game.entities.mains.GravityMan;
import pojahn.game.essentials.Direction;

public class Wind extends MobileEntity { //TODO: Use Earthbound?

	private float power, maxPower;
	private Direction direction;
	private GravityMan[] targets;

	public Wind(float x, float y, float power, float maxPower, Direction direction, GravityMan... targets) {
		move(x, y);
		this.power = power;
		this.maxPower = maxPower;
		this.targets = targets;
		this.direction = direction;
	}
	
	@Override
	public MobileEntity getClone() {
		Wind clone = new Wind(x(),y(),power,maxPower,direction,targets);
		copyData(clone);
		if(cloneEvent != null)
			cloneEvent.handleClonded(clone);
		
		return clone;
	}

	@Override
	public void logistics() {
		for (GravityMan man : targets) {
			if (collidesWith(man)) {
				switch (direction) {
					case N:
						if (man.vel.y < maxPower)
							man.vel.y += power;
						break;
					case NE:
						if (man.vel.y < maxPower)
							man.vel.y += power;
						if (-man.vel.x < maxPower)
							man.vel.x -= power;
						break;
					case E:
						if (-man.vel.x < maxPower)
							man.vel.x -= power;
						break;
					case SE:
						if (-man.vel.y < maxPower)
							man.vel.y -= power;
						if (-man.vel.x < maxPower)
							man.vel.x -= power;
						break;
					case S:
						if (-man.vel.y < maxPower)
							man.vel.y -= power;
						break;
					case SW:
						if (-man.vel.y < maxPower)
							man.vel.y -= power;
						if (man.vel.x < maxPower)
							man.vel.x += power;
						break;
					case W:
						if (man.vel.x < maxPower)
							man.vel.x += power;
						break;
					case NW:
						if (man.vel.y < maxPower)
							man.vel.y += power;
						if (man.vel.x < maxPower)
							man.vel.x += power;
						break;
				}
			}
		}
	}
}
