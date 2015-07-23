package game.entities.mains;

import game.core.PlayableEntity;
import game.essentials.Keystrokes;

public class FlyMan extends PlayableEntity{

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
	}
}
