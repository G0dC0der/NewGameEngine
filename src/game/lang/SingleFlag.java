package game.lang;

public class SingleFlag {

	private boolean pass = true;
	
	public boolean pass(){
		boolean allowed = pass;
		if(pass)
			pass = false;
		return allowed;
	}
	
	public void reset(){
		pass = true;
	}
}
