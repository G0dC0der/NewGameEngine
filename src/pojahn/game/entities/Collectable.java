package pojahn.game.entities;

import pojahn.game.core.Entity;
import pojahn.game.core.MobileEntity;
import pojahn.game.events.Event;

import com.badlogic.gdx.audio.Sound;

public class Collectable extends MobileEntity{

	private Entity[] collectors;
	private Particle collectImage;
	private Event collectEvent;
	private Sound collectSound;
	private boolean collected, disposeCollected;
	
	public Collectable(float x, float y, Entity... collectors){
		this.collectors = collectors;
		disposeCollected = true;
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
					collectSound.play(sounds.calc());
				if(collectImage != null)
					getLevel().add(collectImage.getClone().center(this));
				if(disposeCollected)
					getLevel().discard(this);
				
				break;
			}
		}
	}
}
