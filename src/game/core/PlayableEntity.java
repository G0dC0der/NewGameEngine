package game.core;

import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import game.essentials.Animation;
import game.essentials.Controller;
import game.essentials.Image2D;
import game.essentials.Keystrokes;
import game.essentials.Vitality;

public abstract class PlayableEntity extends MobileEntity{
	
	public static final Keystrokes STILL = new Keystrokes();

	public Animation<Image2D> healthHud, deathImage;
	
	private Keystrokes keysDown;
	private Vitality state;
	private Controller controller;
	private List<Keystrokes> ghostData;
	private int ghostDataCounter, hp, hurtCounter, counter;
	
	public PlayableEntity(){
		hp = 1;
		state = Vitality.ALIVE;
	}
	
	public int getHP(){
		return hp;
	}
	
	public void touch(int strength){
		if(strength >= 0){
			hp += strength;
			hurtCounter = 0;
		}
		else if(hurtCounter > 0){
			hp += strength;
			hurtCounter = 100;	
		}
		
		if(0 >= hp)
			setState(Vitality.DEAD);
	}
	
	public boolean isHurt(){
		return hurtCounter > 0;
	}
	
	public Controller getController(){
		return controller;
	}
	
	public boolean isGhost(){
		return ghostData != null;
	}
	
	public void ghostify(List<Keystrokes> ghostData){
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
	
	public Keystrokes getKeysDown() {
		return keysDown;
	}
	
	@Override
	public void render(SpriteBatch batch) {
		if(--hurtCounter > 0 && ++counter % 0 == 5)
			return;
		
		if(deathImage != null && getState() == Vitality.DEAD){
			Animation<Image2D> realImage = getImage();
			setImage(deathImage);
			super.render(batch);
			setImage(realImage);
		}else
			super.render(batch);
	}
	
	void setKeysDown(Keystrokes keysDown) {
		this.keysDown = keysDown;
	}
	
	public static Keystrokes checkButtons(Controller con){
		Keystrokes pb = new Keystrokes();
		pb.down 	  = Gdx.input.isKeyPressed(con.down);
		pb.left 	  = Gdx.input.isKeyPressed(con.left);
		pb.right 	  = Gdx.input.isKeyPressed(con.right);
		pb.up 		  = Gdx.input.isKeyPressed(con.up);
		pb.jump		  = Gdx.input.isKeyPressed(con.jump);
		pb.pause	  = Gdx.input.isKeyJustPressed(con.pause);
		pb.special1   = Gdx.input.isKeyJustPressed(con.special1);
		pb.special2   = Gdx.input.isKeyJustPressed(con.special2);
		pb.special3   = Gdx.input.isKeyJustPressed(con.special3);
		pb.suicide    = Gdx.input.isKeyJustPressed(con.suicide);
		
		return pb;
	}
	
	public static Keystrokes keysDown(List<PlayableEntity> entities){
		Keystrokes pb = new Keystrokes();

		for(PlayableEntity play : entities){
			if(Gdx.input.isKeyPressed(play.controller.down))
				pb.down = true;
			if(Gdx.input.isKeyPressed(play.controller.left))
				pb.left = true;
			if(Gdx.input.isKeyPressed(play.controller.right))
				pb.right = true;
			if(Gdx.input.isKeyPressed(play.controller.up))
				pb.up = true;
			if(Gdx.input.isKeyPressed(play.controller.jump))
				pb.jump = true;
			if(Gdx.input.isKeyJustPressed(play.controller.pause))
				pb.pause = true;
			if(Gdx.input.isKeyJustPressed(play.controller.special1))
				pb.special1 = true;
			if(Gdx.input.isKeyJustPressed(play.controller.special2))
				pb.special2 = true;
			if(Gdx.input.isKeyJustPressed(play.controller.special3))
				pb.special3 = true;
			if(Gdx.input.isKeyJustPressed(play.controller.suicide))
				pb.suicide = true;
		}
		
		return pb;
	}
	
	final void deathAction() {
		activate(false);
	}
	
	protected void revive(){
		if(deathImage != null)
			deathImage.reset();
		
		activate(true);
	}
	
	Keystrokes nextReplayFrame()
	{
		if(ghostData == null)
			throw new IllegalStateException("This method can only be called if the entity is a ghost.");
		
		return ghostDataCounter > ghostData.size() - 1 ? STILL : ghostData.get(ghostDataCounter++);
	}
}
