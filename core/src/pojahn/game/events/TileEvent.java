package pojahn.game.events;

import pojahn.game.core.Level.Tile;

@FunctionalInterface
public interface TileEvent {

	void eventHandling(Tile tile);
}
