package pojahn.game.essentials;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.LinkedList;
import java.util.List;

import pojahn.game.essentials.Keystrokes.KeystrokeSession;

public class Replay implements Serializable{

	private static final long serialVersionUID = -1915678880790726667L;
	public double time;
	public String playerName, levelClass;
	public ZonedDateTime date;
	public Serializable meta;
	public List<KeystrokeSession> data;
	public GameState result;
	
	public Replay(){
		data = new LinkedList<>();
	}
	
	public boolean hasEnded(){
		for(KeystrokeSession kss : data)
			if(kss.hasEnded())
				return true;
		return false;
	}
}
