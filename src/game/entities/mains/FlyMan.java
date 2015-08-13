package game.entities.mains;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import game.core.PlayableEntity;
import game.essentials.Keystrokes;

public class FlyMan extends PlayableEntity{

	boolean moving, alwaysMove;
	
	@Override
	public void logics() {
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
}
