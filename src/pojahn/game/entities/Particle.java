package pojahn.game.entities;

import com.badlogic.gdx.audio.Sound;
import pojahn.game.core.MobileEntity;

public class Particle extends MobileEntity {

	protected Sound introSound;
	private boolean soundPlayed;
	
	public Particle(){}

	public Particle getClone(){
		Particle clone = new Particle();
		copyData(clone);
		if(cloneEvent != null)
			cloneEvent.handleClonded(clone);
		
		return clone;
	}
	
	public void setIntroSound(Sound introSound){
		this.introSound = introSound;
	}
	
	@Override
	public void logistics() {
		if(!soundPlayed && introSound != null){
			soundPlayed = true;
			introSound.play(sounds.calc());
		}
		
		if(!isVisible() || getImage().hasEnded())
			getLevel().discard(this);
	}
	
	protected void copyData(Particle clone){
		super.copyData(clone);
		clone.introSound = introSound;
	}
}