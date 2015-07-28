package game.essentials;

import game.core.Collisions;
import game.core.Entity;

public class SoundEmitter{

	public float power, maxDistance, maxVolume;
	public boolean useFalloff;
	private final Entity emitter;
	
	public SoundEmitter(Entity emitter){
		this.emitter = emitter;
		power = 20;
		maxDistance = 700;
		maxVolume = 1.0f;
	}
	
	public float calc(){
		if(!useFalloff)
			return maxVolume;

		return calc(Collisions.findClosest(emitter, emitter.getLevel().getSoundListeners()));
	}
	
	public float calc(Entity listener){
		return calc(listener.x(), listener.y());
	}
	
	public float calc(float listenerX, float listenerY){
		if(!useFalloff)
			return maxVolume;
		
		double distance = Collisions.distance(emitter.x(), emitter.y(), listenerX, listenerY);
		float candidate = (float) (power * Math.max((1 / Math.sqrt(distance)) - (1 / Math.sqrt(maxDistance)), 0));
		
		return Math.min(candidate, maxVolume);
	}
}