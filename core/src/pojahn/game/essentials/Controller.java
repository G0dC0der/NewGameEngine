package pojahn.game.essentials;

import com.badlogic.gdx.Input.Keys;

public class Controller {

    public static final Controller DEFAULT_CONTROLLER = new Controller(Keys.UP, Keys.DOWN, Keys.LEFT, Keys.RIGHT, Keys.SPACE, Keys.ESCAPE, Keys.Q, Keys.NUM_1, Keys.NUM_2, Keys.NUM_2, Keys.R, Keys.Q);
    public static final Controller DEBUG_CONTROLLER = new Controller(Keys.W, Keys.S, Keys.A, Keys.D, Keys.W, Keys.BACKSPACE, Keys.ENTER, Keys.Z, Keys.X, Keys.C, Keys.R, Keys.Q);

    public int up, down, left, right, jump, pause, suicide, special1, special2, special3, restart, quit;

    public Controller(final int up, final int down, final int left, final int right, final int jump, final int pause, final int suicide, final int special1, final int special2, final int special3, final int restart, final int quit) {
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
        this.restart = restart;
        this.quit = quit;
    }
}