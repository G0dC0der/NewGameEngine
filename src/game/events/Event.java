package game.events;

@FunctionalInterface
public interface Event {

	void eventHandling();
	
	default boolean isDone(){
		return false;
	}
}
