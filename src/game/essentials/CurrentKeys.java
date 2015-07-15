package game.essentials;

import java.util.List;

public class CurrentKeys {
	
	public boolean up, down, left, right, jump, pause, special1, special2, special3;

	public CurrentKeys(boolean up, boolean down, boolean left, boolean right, boolean jump, boolean pause, 
				boolean special1, boolean special2, boolean special3) {
		this.up = up;
		this.down = down;
		this.left = left;
		this.right = right;
		this.jump = jump;
		this.pause = pause;
		this.special1 = special1;
		this.special2 = special2;
		this.special3 = special3;
	}
	
	public static class MultiCurrentKeys{
		public List<CurrentKeys> data;
	}
}