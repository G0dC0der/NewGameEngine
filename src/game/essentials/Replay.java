package game.essentials;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.LinkedList;
import java.util.List;

import game.essentials.PressedButtons.PressedButtonsSession;

public class Replay {

	public double time;
	public String replayName, levelClass;
	public ZonedDateTime date;
	public Serializable meta;
	public List<PressedButtonsSession> data;
	
	public Replay(){
		data = new LinkedList<>();
	}
}
