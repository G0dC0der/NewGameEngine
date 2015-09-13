package pojahn.game.entities;

import com.badlogic.gdx.audio.Sound;

import pojahn.game.core.MobileEntity;
import pojahn.game.essentials.Animation;
import pojahn.game.essentials.Image2D;

public class DestroyablePlatform extends SolidPlatform {
	
	private Animation<Image2D> destroyImage;
	private int destroyFrames, aliveCounter;
	private Sound breakSound, destroySound;

	public DestroyablePlatform(float x, float y, MobileEntity... subjects) {
		super(x, y, subjects);
		destroyFrames = 100;
		aliveCounter = -1;
	}
	
	public DestroyablePlatform getClone() {
		DestroyablePlatform clone = new DestroyablePlatform(x(), y(), subjects.toArray(new MobileEntity[subjects.size()]));
		copyData(clone);
		if(cloneEvent != null)
			cloneEvent.handleClonded(clone);
		
		return clone;
	}
	
	@Override
	public void logics() {
		super.logics();
		
		if(aliveCounter-- == 0){
			if(destroySound != null)
				destroySound.play(sounds.calc());
			getLevel().discard(this);
		} else if(aliveCounter < 0){ //Not collapsing
			if(!getInteractingSubjects().isEmpty())
				collapse();
		}
	}

	public void collapse(){
		aliveCounter = destroyFrames;
		if(breakSound != null)
			breakSound.play(sounds.calc());
		if(destroyImage != null)
			setImage(destroyImage);
	}
	
	public void setBreakSound(Sound sound){
		this.breakSound = sound;
	}
	
	public void setDestroySound(Sound sound){
		this.destroySound = sound;
	}
	
	public void setDestroyImage(Animation<Image2D> destroyImage){
		this.destroyImage = destroyImage;
	}
	
	protected void copyData(DestroyablePlatform clone) {
		super.copyData(clone);
		if(destroyImage != null)
			clone.destroyImage = destroyImage.getClone();
		clone.destroyFrames = destroyFrames;
		clone.breakSound = breakSound;
		clone.destroySound = destroySound;
	}
}
