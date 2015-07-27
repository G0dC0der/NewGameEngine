package game.entities;

import com.badlogic.gdx.audio.Sound;

import game.core.Entity;
import game.core.MobileEntity;
import game.events.Event;

public class Collectable extends MobileEntity{

	private Entity[] collectors;
	private Particle collectImage;
	private Event collectEvent;
	private Sound collectSound;
	private boolean collected, disposeCollected;
	
	public Collectable(float x, float y, Entity... collectors){
		this.collectors = collectors;
		move(x,y);
	}
	
	public void setCollectSound(Sound collectSound){
		this.collectSound = collectSound;
	}
	
	/**
	 * The {@code Event} to execute upon collection.
	 */
	public void setCollectEvent(Event collectEvent){
		this.collectEvent = collectEvent;
	}
	
	public void disposeCollected(boolean disposeCollected){
		this.disposeCollected = disposeCollected;
	}
	
	@Override
	public void logics() {
		if(collected)
			return;
			
		for(Entity collector : collectors){
			if(collidesWith(collector)){
				collected = true;
				if(collectEvent != null)
					collectEvent.eventHandling();
				if(collectSound != null)
					collectSound.play();//TODO: Calculate volume
				if(collectImage != null)
					getLevel().add(new Particle(collectImage, x(),y()));
				if(disposeCollected)
					getLevel().discard(this);
			}
		}
	}
}
