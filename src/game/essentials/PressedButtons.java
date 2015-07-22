package game.essentials;

import java.util.LinkedList;
import java.util.List;

import game.core.PlayableEntity;

/**
 * An instance of this class describes the buttons that was down during a frame.
 * @author Pojahn M
 *
 */
public class PressedButtons {
	
	public boolean up, down, left, right, jump, pause, suicide, special1, special2, special3;
	
	public PressedButtons() {}

	public PressedButtons(boolean up, boolean down, boolean left, boolean right, boolean jump, boolean pause, boolean suicide,
				boolean special1, boolean special2, boolean special3) {
		this.up = up;
		this.down = down;
		this.left = left;
		this.right = right;
		this.jump = jump;
		this.pause = pause;
		this.suicide = suicide;
		this.special1 = special1;
		this.special2 = special2;
		this.special3 = special3;
	}
	
	/**
	 * An instance of this class describes the buttons that a specific PlayableEntity held down during a session(from start to GameState.COMPLETE).
	 * @author Pojahn M
	 */
	public static class PressedButtonsSession{
		public List<PressedButtons> sessionKeys;
		public String id;
		private int counter;
		
		public PressedButtonsSession(String id) {
			this.sessionKeys = new LinkedList<>();
			this.id = id;
		}
		
		public PressedButtons next(){
			return counter > sessionKeys.size() - 1 ? PlayableEntity.STILL : sessionKeys.get(counter++);
		}
	}
}