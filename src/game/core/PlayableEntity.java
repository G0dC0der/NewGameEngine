package game.core;

import java.util.List;

import com.badlogic.gdx.Gdx;

import game.essentials.Controller;
import game.essentials.Image2D;
import game.essentials.PressedButtons;
import game.essentials.Vitality;

public class PlayableEntity extends MobileEntity{
	
	public static final PressedButtons STILL = new PressedButtons();

	public Image2D healthHud;
	
	private PressedButtons keysDown;
	private Vitality state;
	private Controller controller;
	private List<PressedButtons> ghostData;
	private int ghostDataCounter, hp;
	
	public int getHP(){
		return hp;
	}
	
	public void touch(int  hp){
		
	}
	
	public Controller getController(){
		return controller;
	}
	
	public boolean isGhost(){
		return ghostData != null;
	}
	
	public void ghostify(List<PressedButtons> ghostData){
		this.ghostData = ghostData;
	}
	
	public void setState(Vitality state){
		if(state == Vitality.ALIVE && this.state == Vitality.DEAD)
			revive();
		if(state == Vitality.ALIVE && this.state == Vitality.COMPLETED)
			throw new IllegalArgumentException("Can not set to state to " + Vitality.ALIVE + " when the current state is " + Vitality.COMPLETED);
			
		this.state = state;
	}
	
	public Vitality getState(){
		return state;
	}
	
	public PressedButtons getKeysDown() {
		return keysDown;
	}
	
	public void setKeysDown(PressedButtons keysDown) {
		this.keysDown = keysDown;
	}
	
	public static PressedButtons checkButtons(Controller con){
		PressedButtons pb = new PressedButtons();
		pb.down 	  = Gdx.input.isKeyPressed(con.down);
		pb.left 	  = Gdx.input.isKeyPressed(con.left);
		pb.right 	  = Gdx.input.isKeyPressed(con.right);
		pb.up 		  = Gdx.input.isKeyPressed(con.up);
		pb.pause	  = Gdx.input.isKeyJustPressed(con.pause);
		pb.special1   = Gdx.input.isKeyJustPressed(con.special1);
		pb.special2   = Gdx.input.isKeyJustPressed(con.special2);
		pb.special3   = Gdx.input.isKeyJustPressed(con.special3);
		pb.suicide    = Gdx.input.isKeyJustPressed(con.suicide);
		
		return pb;
	}
	
	protected void revive(){
		
	}
	
	PressedButtons nextReplayFrame()
	{
		if(ghostData == null)
			throw new IllegalStateException("This method can only be called if the entity is a ghost.");
		
		return ghostDataCounter > ghostData.size() - 1 ? STILL : ghostData.get(ghostDataCounter++);
	}
}
