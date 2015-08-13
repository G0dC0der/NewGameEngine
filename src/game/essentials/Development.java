package game.essentials;

import game.core.Entity;
import game.core.PlayableEntity;
import game.events.Event;

public class Development {

	public static Event debugMovement(PlayableEntity play){
		return ()->{
			Keystrokes strokes = play.getKeysDown();
			if(strokes.up || strokes.jump)
				System.out.println("Can go up: " + !play.occupiedAt(play.x(), play.y() - 1));
			else if(strokes.down)
				System.out.println("Can go down: " + !play.occupiedAt(play.x(), play.height() + play.y() + 1));
			
			if(strokes.left)
				System.out.println("Can go left: " + !play.occupiedAt(play.x() - 1, play.y()));
			else if(strokes.right)
				System.out.println("Can go right: " + !play.occupiedAt(play.width() + play.x() + 1, play.y()));
		};
	}
	
	public static Event debugPosition(Entity entity){
		return()->{
			System.out.println(entity.x() + " " + entity.y());
		};
	}
}
