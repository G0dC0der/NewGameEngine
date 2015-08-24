package game.essentials;

import com.badlogic.gdx.Input.Keys;

public class Controller {
	
	public static final Controller DEFAULT_CONTROLLER = new Controller(Keys.UP, Keys.DOWN, Keys.LEFT, Keys.RIGHT, Keys.SPACE, Keys.ESCAPE, Keys.Q, Keys.NUM_1, Keys.NUM_2, Keys.NUM_2);
	public static final Controller DEBUG_CONTROLLER = new Controller(Keys.W, Keys.S, Keys.A, Keys.D, Keys.W, Keys.BACKSPACE, Keys.ENTER, Keys.Z, Keys.X, Keys.C);

	public int up, down, left, right, jump, pause, suicide, special1, special2, special3;

	public Controller(int up, int down, int left, int right, int jump, int pause, int suicide, int special1, int special2, int special3) {
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
}