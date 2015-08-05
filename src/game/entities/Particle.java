package game.entities;

import com.badlogic.gdx.audio.Sound;

import game.core.MobileEntity;

public class Particle extends MobileEntity{

	private Sound introSound;
	private boolean soundPlayed;
	
	public Particle(){}
	
	public Particle(Particle src){
		this.introSound = src.introSound;
		copyData(src);
		if(src.cloneEvent != null)
			src.cloneEvent.handleClonded(this);
	}
	
	public void setIntroSound(Sound introSound){
		this.introSound = introSound;
	}
	
	@Override
	public void logics() {
		if(!soundPlayed && introSound != null){
			soundPlayed = true;
			introSound.play(sounds.calc());
		}
		
		if(!isVisible() || getImage().hasEnded())
			getLevel().discard(this);
	}
}
