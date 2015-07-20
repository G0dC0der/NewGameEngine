package game.essentials;

import game.core.Entity;

@FunctionalInterface
public interface CloneEvent {

	void handleClonded(Entity cloned);
}
