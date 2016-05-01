package pojahn.game.entities;

import com.badlogic.gdx.audio.Sound;

import pojahn.game.core.Entity;
import pojahn.game.core.MobileEntity;
import pojahn.game.events.Event;

public class Collectable extends MobileEntity{
	
	@FunctionalInterface
	public interface CollectEvent{
		void eventHandling(Entity collector);
	}

	private Entity[] collectors;
	private Entity subject;
	private Particle collectImage;
	private CollectEvent collectEvent;
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

	public void setCollectEvent(CollectEvent collectEvent){
		this.collectEvent = collectEvent;
	}
	
	public void disposeCollected(boolean disposeCollected){
		this.disposeCollected = disposeCollected;
	}
	
	public Entity getSubject(){
		return subject;
	}
	
	@Override
	public void logics() {
		if(collected) {
			for(Entity collector : collectors){
				if(collidesWith(collector)){
					collected = true;
					subject = collector;
					if(collectEvent != null)
						collectEvent.eventHandling(collector);
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
	
	public static class CollectEvents{
		public static CollectEvent wrap(Event event){
			return collector -> event.eventHandling() ;
		}
		
		public static CollectEvent freeze(int frames){
			return collector->{
				MobileEntity mobile = (MobileEntity) collector;
				mobile.freeze();
				mobile.getLevel().runOnceAfter(()->mobile.unfreeze(), frames);
			};
		}
		
		public static CollectEvent speed(int frames, float multiplier){
			return collector->{
				MobileEntity mobile = (MobileEntity) collector;
				float orgSpeed = mobile.getMoveSpeed();
				mobile.setMoveSpeed(orgSpeed * multiplier);
				mobile.getLevel().runOnceAfter(()-> mobile.setMoveSpeed(orgSpeed), frames);
			};
		}
	}
}
