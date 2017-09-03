package pojahn.game.events;

import pojahn.lang.Bool;

public class ChainEvent {

    private Event event;

    public void then(Event event) {
        this.event = event;
    }

    public void thenRunOnce(Event event) {
        Bool bool = new Bool();
        this.event = ()-> {
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
