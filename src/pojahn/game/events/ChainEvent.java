package pojahn.game.events;

public class ChainEvent {

    private ActionEvent event;

    public void then(ActionEvent event) {
        this.event = event;
    }

    public ActionEvent getEvent() {
        return event;
    }
}
