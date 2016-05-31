package pojahn.game.essentials;

import com.badlogic.gdx.Gdx;

import java.io.Serializable;

/**
 * An instance of this class describes the buttons that was down during a frame.
 *
 * @author Pojahn M
 */
public class Keystrokes implements Serializable {

    private static final long serialVersionUID = -7757124776060327750L;
    public static final Keystrokes AFK = new Keystrokes();

    public boolean up, down, left, right, jump, pause, suicide, special1, special2, special3, restart, quit;

    @Override
    public String toString() {
        return "Keystrokes{" +
                "up=" + up +
                ", down=" + down +
                ", left=" + left +
                ", right=" + right +
                ", jump=" + jump +
                ", pause=" + pause +
                ", suicide=" + suicide +
                ", special1=" + special1 +
                ", special2=" + special2 +
                ", special3=" + special3 +
                ", restart=" + restart +
                ", quit=" + quit +
                '}';
    }

    /**
     * Creates a new Keystrokes based on what buttons are currently down.
     */
    public static Keystrokes from(Controller con) {
        Keystrokes ks = new Keystrokes();
        ks.down = Gdx.input.isKeyPressed(con.down);
        ks.left = Gdx.input.isKeyPressed(con.left);
        ks.right = Gdx.input.isKeyPressed(con.right);
        ks.up = Gdx.input.isKeyPressed(con.up);
        ks.jump = Gdx.input.isKeyPressed(con.jump);
        ks.pause = Gdx.input.isKeyJustPressed(con.pause);
        ks.special1 = Gdx.input.isKeyJustPressed(con.special1);
        ks.special2 = Gdx.input.isKeyJustPressed(con.special2);
        ks.special3 = Gdx.input.isKeyJustPressed(con.special3);
        ks.suicide = Gdx.input.isKeyJustPressed(con.suicide);
        ks.restart = Gdx.input.isKeyJustPressed(con.restart);
        ks.quit = Gdx.input.isKeyJustPressed(con.quit);

        return ks;
    }

    public static Keystrokes merge(Keystrokes ks1, Keystrokes ks2) {
        Keystrokes pb = new Keystrokes();

        pb.down = ks1.down || ks2.down;
        pb.left = ks1.left || ks2.left;
        pb.right = ks1.right || ks2.right;
        pb.up = ks1.up || ks2.up;
        pb.jump = ks1.jump || ks2.jump;
        pb.pause = ks1.pause || ks2.pause;
        pb.quit = ks1.quit || ks2.quit;
        pb.special1 = ks1.special1 || ks2.special1;
        pb.special2 = ks1.special2 || ks2.special2;
        pb.special3 = ks1.special3 || ks2.special3;
        pb.suicide = ks1.suicide || ks2.suicide;

        return pb;
    }
}