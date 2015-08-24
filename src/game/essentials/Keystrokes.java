package game.essentials;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import game.core.PlayableEntity;

/**
 * An instance of this class describes the buttons that was down during a frame.
 * @author Pojahn M
 *
 */
public class Keystrokes implements Serializable{
	
	private static final long serialVersionUID = -7757124776060327750L;
	public boolean up, down, left, right, jump, pause, suicide, special1, special2, special3;
	
	public Keystrokes() {}

	public Keystrokes(boolean up, boolean down, boolean left, boolean right, boolean jump, boolean pause, boolean suicide,
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
	
	@Override
	public String toString() {
		return String.format("Up: %b, Down: %b, Left: %b, Right: %b, Jump: %b, Pause: %b, Suicide: %b, Special1: %b, Special2: %b, Special3: %b", up, down, left, right, jump, pause, suicide, special1, special2, special3);
	}
	
	/**
	 * An instance of this class describes the buttons that a specific PlayableEntity held down during a session(from start to GameState.FINISHED).
	 * @author Pojahn M
	 */
	public static class KeystrokesSession implements Serializable{

		private static final long serialVersionUID = -7482346021676423243L;
		public List<Keystrokes> sessionKeys;
		public String id;
		private int counter;
		
		public KeystrokesSession(String id) {
			this.sessionKeys = new LinkedList<>();
			this.id = id;
		}
		
		public boolean hasEnded(){
			return counter > sessionKeys.size() - 1;
		}
		
		public Keystrokes next(){
			return counter > sessionKeys.size() - 1 ? PlayableEntity.STILL : sessionKeys.get(counter++);
		}
		
		public void reset(){
			counter = 0;
		}
	}
}