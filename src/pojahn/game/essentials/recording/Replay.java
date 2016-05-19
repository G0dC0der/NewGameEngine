package pojahn.game.essentials.recording;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class Replay implements Serializable{

	private static final long serialVersionUID = -1915678880790726667L;
	public double time;
	public int deaths;
	public String playerName, levelName;
	public ZonedDateTime date;
	public Serializable meta;
	public List<KeySession> keystrokes;

	public Replay(){
		keystrokes = new ArrayList<>();
	}
}
