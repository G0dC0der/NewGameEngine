package pojahn.game.events;

import pojahn.game.core.Entity;

@FunctionalInterface
public interface CloneEvent {

	void handleClonded(Entity clonie);
}
