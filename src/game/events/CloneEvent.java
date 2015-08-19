package game.events;

import game.core.Entity;

@FunctionalInterface
public interface CloneEvent {

	void handleClonded(Entity clonie);
}
