package pojahn.game.essentials;

public class FutureObject<T> {

    private T obj;

    public synchronized void set(T obj) {
        this.obj = obj;
        notifyAll();
    }

    public synchronized T get() {
        return obj;
    }

    public synchronized T deferGet() {
        while (obj == null) {
            try {
                wait();
            } catch (InterruptedException e) {}
        }

        return obj;
    }
}
