package pojahn.game.entities.mains;

import pojahn.game.core.PlayableEntity;
import pojahn.game.essentials.Keystrokes;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class FlyMan extends PlayableEntity{

	boolean moving, alwaysMove;

	@Override
	public FlyMan getClone(){
		FlyMan clone = new FlyMan();
		copyData(clone);
		if(cloneEvent != null)
			cloneEvent.handleClonded(clone);
		
		return clone;
	}
	
	@Override
	public void logistics() {
		Keystrokes strokes = getKeysDown();
		int speed = (int) getMoveSpeed();
		
		if(strokes.up || strokes.jump)
			tryUp(speed);
		else if(strokes.down)
			tryDown(speed);
		
		if(strokes.left)
			tryLeft(speed);
		else if(strokes.right)
			tryRight(speed);
		
		moving = alwaysMove || strokes.up || strokes.left || strokes.right || strokes.down || strokes.jump;
	}
	
	public void alwaysMove(boolean alwaysMove){
		this.alwaysMove = alwaysMove;
	}
	
	@Override
	public void render(SpriteBatch batch) {
		getImage().stop(!moving);
		super.render(batch);
	}
	
	protected void copyData(FlyMan clone){
		super.copyData(clone);
		clone.alwaysMove = alwaysMove;
	}
}
