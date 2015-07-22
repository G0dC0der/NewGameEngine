package game.essentials;

import game.core.Collisions;
import game.core.Entity;

public interface SoundEmitter{

	float getMaxVolume();
	
	void setMaxVolume(float maxVolume);
	
	float getMaxDistance();
	
	void setMaxDistance(float maxDistance);
	
	float getPower();
	
	void setPower(float power);
	
	float x();
	
	float y();
	
	default float calc(Entity listener){
		double distance = Collisions.distance(x(), y(), listener.centerX(), listener.centerY());
		float candidate = (float) (getPower() * Math.max((1 / Math.sqrt(distance)) - (1 / Math.sqrt(getMaxDistance())), 0));
		
		return Math.min(candidate, getMaxVolume());
	}
}