package pojahn.game.entities;

import pojahn.game.core.MobileEntity;
import pojahn.game.essentials.Direction;

public class OneWay extends MobileEntity {
	
	private MobileEntity targets[];
	private boolean[] block;
	private Direction direction;

	public OneWay(float x, float y, Direction direction, MobileEntity... targets) {
		if (direction == Direction.NE || direction == Direction.SE || direction == Direction.SW
				|| direction == Direction.NW)
			throw new IllegalArgumentException("The direction must be either N, S, W or E.");

		move(x, y);
		this.direction = direction;
		this.targets = targets;
		block = new boolean[targets.length];
	}

	@Override
	public void logics() {
		for (int i = 0; i < targets.length; i++) {
			MobileEntity mobile = targets[i];
			boolean bool;

			switch (direction) {
				case S:
					bool = mobile.y() >= y() + height();
					break;
				case N:
					bool = mobile.y() + mobile.height() <= y();
					break;
				case E:
					bool = x() + width() <= mobile.x();
					break;
				case W:
					bool = x() >= mobile.x() + mobile.width();
					break;
				default:
					throw new RuntimeException();
			}

			if (bool) {
				if (!block[i]) {
					block[i] = true;
					mobile.addObstacle(this);
				}
			} else if (block[i]) {
				block[i] = false;
				mobile.removeObstacle(this);
			}
		}
	}

	public OneWay getClone(float x, float y) {
		OneWay clone = new OneWay(x, y, direction, targets);
		copyData(clone);

		if (cloneEvent != null)
			cloneEvent.handleClonded(clone);

		return clone;
	}
}