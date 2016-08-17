package pojahn.game.events;

public class ChainEvent {

    private Event event;

    public void then(Event event) {
        this.event = event;
    }

    public Event getEvent() {
        return event;
    }
}
