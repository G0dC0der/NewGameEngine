package game.entities;

import com.badlogic.gdx.audio.Sound;

import game.core.MobileEntity;

public class Particle extends MobileEntity{

	private Sound introSound;
	private boolean soundPlayed;
	
	public Particle(){}
	
	public Particle(Particle mother, float x, float y){
		this.introSound = mother.introSound;
		copyData(mother);
		move(x,y);
	}
	
	public void setIntroSound(Sound introSound){
		this.introSound = introSound;
	}
	
	@Override
	public void logics() {
		if(!soundPlayed && introSound != null){
			soundPlayed = true;
			introSound.play();//TODO: Calc volume
		}
		
		if(!isVisible() || getImage().getIndex() >= getImage().getArray().length - 2)
			getLevel().discard(this);
	}
}
