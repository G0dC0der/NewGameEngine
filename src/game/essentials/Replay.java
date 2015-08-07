package game.essentials;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.LinkedList;
import java.util.List;

import game.essentials.Keystrokes.KeystrokesSession;

public class Replay {

	public double time;
	public String playerName, levelClass;
	public ZonedDateTime date;
	public Serializable meta;
	public List<KeystrokesSession> data;
	public GameState result;
	
	public Replay(){
		data = new LinkedList<>();
	}
}
