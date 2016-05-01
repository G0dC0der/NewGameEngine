package pojahn.game.essentials;

import com.badlogic.gdx.Gdx;
import pojahn.game.core.PlayableEntity;

import java.io.Serializable;
import java.util.List;

/**
 * An instance of this class describes the buttons that was down during a frame.
 * @author Pojahn M
 */
public class Keystrokes implements Serializable{
	
	private static final long serialVersionUID = -7757124776060327750L;
	public boolean up, down, left, right, jump, pause, suicide, special1, special2, special3;

	@Override
	public String toString() {
		return String.format("Up: %b, Down: %b, Left: %b, Right: %b, Jump: %b, Pause: %b, Suicide: %b, Special1: %b, Special2: %b, Special3: %b", up, down, left, right, jump, pause, suicide, special1, special2, special3);
	}

	/**
	 * Creates a new Keystrokes based on what buttons are currently down.
     */
	public static Keystrokes from(Controller con){
		Keystrokes pb = new Keystrokes();
		pb.down 	  = Gdx.input.isKeyPressed(con.down);
		pb.left 	  = Gdx.input.isKeyPressed(con.left);
		pb.right 	  = Gdx.input.isKeyPressed(con.right);
		pb.up 		  = Gdx.input.isKeyPressed(con.up);
		pb.jump		  = Gdx.input.isKeyJustPressed(con.jump);
		pb.pause	  = Gdx.input.isKeyJustPressed(con.pause);
		pb.special1   = Gdx.input.isKeyJustPressed(con.special1);
		pb.special2   = Gdx.input.isKeyJustPressed(con.special2);
		pb.special3   = Gdx.input.isKeyJustPressed(con.special3);
		pb.suicide    = Gdx.input.isKeyJustPressed(con.suicide);

		return pb;
	}
}