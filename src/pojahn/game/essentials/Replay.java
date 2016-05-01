package pojahn.game.essentials;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class Replay implements Serializable{

	private static final long serialVersionUID = -1915678880790726667L;
	public double time;
	public String playerName, levelClass;
	public ZonedDateTime date;
	public Serializable meta;
	public List<Keystrokes.KeystrokeSession> keystrokes;
	public GameState result;
	
	public Replay(){
		keystrokes = new ArrayList<>();
	}
}
