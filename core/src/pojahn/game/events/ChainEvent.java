package pojahn.game.events;

import pojahn.lang.Bool;

public class ChainEvent {

    private Event event;

    public void then(final Event event) {
        this.event = event;
    }

    public void thenRunOnce(final Event event) {
        final Bool bool = new Bool();
        this.event = () -> {
            if (!bool.value) {
                event.eventHandling();
                bool.value = true;
            }
        };
    }

    public Event getEvent() {
        return event;
    }
}
