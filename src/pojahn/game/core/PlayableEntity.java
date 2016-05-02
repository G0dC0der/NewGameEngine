package pojahn.game.core;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import pojahn.game.entities.Particle;
import pojahn.game.essentials.Animation;
import pojahn.game.essentials.Controller;
import pojahn.game.essentials.Image2D;
import pojahn.game.essentials.Keystrokes;
import pojahn.game.essentials.Vitality;

import java.util.LinkedList;
import java.util.List;

public abstract class PlayableEntity extends MobileEntity{
	
	public static final Keystrokes STILL = new Keystrokes();

	public Animation<Image2D> healthHud;
	public Particle deathImage;

	private Sound hurtSound;
	private Keystrokes keysDown;
	private Vitality state;
	private Controller controller;
	private List<Keystrokes> replayData;
	private int replayDataCounter, hp, hurtCounter, counter;
    private boolean isGhost;

	public PlayableEntity(){
		hp = 1;
		state = Vitality.ALIVE;
        replayData = new LinkedList<>();
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
            if(hurtSound != null)
                hurtSound.play(sounds.calc());
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
	
	public void setController(Controller controller){
		this.controller = controller;
	}

    public Sound getHurtSound() {
        return hurtSound;
    }

    public void setHurtSound(Sound hurtSound) {
        this.hurtSound = hurtSound;
    }

    public boolean isGhost(){
		return isGhost;
	}

    public void setGhostData(List<Keystrokes> replayData) {
        this.replayData = replayData;
        isGhost = true;
    }

    void addReplayFrame(Keystrokes keystrokes) {
        replayData.add(keystrokes);
    }

    void setReplayData(List<Keystrokes> replayData) {
        this.replayData = replayData;
    }

    List<Keystrokes> getReplayData() {
        return replayData;
    }
	
	public void setState(Vitality state){
		if(state == Vitality.ALIVE && this.state == Vitality.COMPLETED)
			throw new IllegalArgumentException("Can not set to state to " + Vitality.ALIVE + " when the current state is " + Vitality.COMPLETED);
		if(state == Vitality.DEAD && this.state == Vitality.DEAD)
			throw new IllegalArgumentException("Can not kill an alrady dead character");

		if(state == Vitality.ALIVE && this.state == Vitality.DEAD)
			revive();

		this.state = state;
		
		if(this.state == Vitality.DEAD)
			deathAction();
	}
	
	public Vitality getState(){
		return state;
	}

    public boolean isAlive() {
        return getState() == Vitality.ALIVE;
    }

    public boolean isDead() {
        return getState() == Vitality.DEAD;
    }

    public boolean isDone() {
        return getState() == Vitality.COMPLETED;
    }
	
	public Keystrokes getKeysDown() {
		return keysDown;
	}
	
	@Override
	public void render(SpriteBatch batch) {
		if(--hurtCounter > 0 && ++counter % 5 == 0)
			return;
		
		super.render(batch);
	}
	
	void setKeysDown(Keystrokes keysDown) {
		this.keysDown = keysDown;
	}
	
	public static Keystrokes mergeButtons(List<PlayableEntity> entities){
		Keystrokes pb = new Keystrokes();

		for(PlayableEntity play : entities){
			Keystrokes playerStrokes = Keystrokes.from(play.controller);

			pb.down = playerStrokes.down || pb.down;
			pb.left = playerStrokes.left || pb.left;
			pb.right = playerStrokes.right || pb.right;
			pb.up = playerStrokes.up || pb.up;
			pb.jump = playerStrokes.jump || pb.jump;
			pb.pause = playerStrokes.pause || pb.pause;
			pb.special1 = playerStrokes.special1 || pb.special1;
			pb.special2 = playerStrokes.special2 || pb.special2;
			pb.special3 = playerStrokes.special3 || pb.special3;
			pb.suicide = playerStrokes.suicide || pb.suicide;
		}
		
		return pb;
	}
	
	private void deathAction() {
		activate(false);
		setVisible(false);
		if(deathImage != null)
			getLevel().add(deathImage.getClone().center(this));
	}
	
	private void revive(){
		activate(true);
		setVisible(true);
	}

	boolean hasEnded() {
		return replayDataCounter < replayData.size() - 1;
	}
	
	Keystrokes nextInput(){
		if(isGhost || engine.isReplaying()) {
		    return hasEnded() ? STILL : replayData.get(replayDataCounter++);
        } else {
			throw new IllegalStateException("This method can only be called if the entity is a ghost or replaying.");
        }
	}
}
