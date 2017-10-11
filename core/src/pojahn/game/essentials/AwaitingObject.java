package pojahn.game.essentials;

public class AwaitingObject<T> {

    private int counter;
    private final T object;

    public AwaitingObject(final int time, final T object) {
        this.counter = time;
        this.object = object;
    }

    public boolean tick() {
        return --counter <= 0;
    }

    public T unwrap() {
        if (counter > 0) {
            throw new IllegalStateException("Can't unwrap unless counter reached zero.");
        }

        return object;
    }
}
