package pojahn.game.events;

import pojahn.game.core.Entity;

@FunctionalInterface
public interface ActionEvent {

	void eventHandling(Entity caller);
}
