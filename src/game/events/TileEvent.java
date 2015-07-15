package game.events;

@FunctionalInterface
public interface TileEvent {

	void eventHandling(byte tile);
}
