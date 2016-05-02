package pojahn.game.entities;

import com.badlogic.gdx.audio.Sound;

import pojahn.game.core.Collisions;
import pojahn.game.core.Entity;

public class Guard extends PathDrone{

	private Entity[] targets;
	private Sound detectSound;
	private boolean allowSound;
	
	public Guard(float x, float y, Entity... targets) {
		super(x, y);
		this.targets = targets;
		allowSound = true;
	}
	
	public boolean isHunting(){
		return !allowSound;
	}

	public void setDetectSound(Sound detectSound) {
		this.detectSound = detectSound;
	}

	@Override
	public void logistics() {
		Entity target = Collisions.findClosestSeeable(this, targets);
		
		if(target != null){
			moveTowards(target.x(), target.y());
			if(allowSound && detectSound != null)
				detectSound.play(sounds.calc());
			allowSound = false;
		} else {
			super.logistics();
			allowSound = true;
		}
	}
}
