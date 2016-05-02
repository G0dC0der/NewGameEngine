package pojahn.game.essentials;

import pojahn.game.core.Collisions;
import pojahn.game.core.MobileEntity;

public enum Direction {
	N,
	NE,
	E,
	SE,
	S,
	SW,
	W,
	NW;

	public static boolean isDiagonal(Direction dir) {
		switch (dir) {
			case NE:
			case SE:
			case SW:
			case NW:
				return true;
			default:
				return false;
		}
	}

	public static Direction invert(Direction dir){
		switch(dir){
			case N:
				return Direction.S;
			case NE:
				return Direction.SW;
			case E:
				return Direction.W;
			case SE:
				return Direction.NW;
			case S:
				return Direction.N;
			case SW:
				return Direction.NE;
			case W:
				return Direction.E;
			case NW:
				return Direction.SE;
			default:
				throw new RuntimeException();
		}
	}
}
