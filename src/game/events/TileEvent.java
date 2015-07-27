package game.events;

import game.core.Level.Tile;

@FunctionalInterface
public interface TileEvent {

	void eventHandling(Tile tile);
}
