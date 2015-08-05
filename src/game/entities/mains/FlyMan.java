package game.entities.mains;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import game.core.PlayableEntity;
import game.essentials.Keystrokes;

public class FlyMan extends PlayableEntity{

	boolean moving, alwaysMove;
	
	@Override
	public void logics() {
		Keystrokes strokes = getKeysDown();
		
		if(strokes.up || strokes.jump)
			tryUp((int)getMoveSpeed());
		else if(strokes.down)
			tryDown((int)getMoveSpeed());
		
		if(strokes.left)
			tryLeft((int)getMoveSpeed());
		else if(strokes.right)
			tryRight((int)getMoveSpeed());
		
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
