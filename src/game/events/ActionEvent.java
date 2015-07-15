package game.events;

import game.core.Entity;

@FunctionalInterface
public interface ActionEvent {

	void eventHandling(Entity caller);
}
