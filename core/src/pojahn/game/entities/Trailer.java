package pojahn.game.entities;

import pojahn.game.core.Entity;
import pojahn.game.entities.movement.PathDrone;
import pojahn.lang.Obj;

import java.util.List;

public class Trailer extends PathDrone {

    private List<Entity> trailers;
    private final int freq;
    private int counter, counter2, spitCounter, limit;
    private boolean stop;

    public Trailer(final float x, final float y, final int freq, final Entity... trailers) {
        super(x, y);
        this.freq = freq;
        this.trailers = List.of(trailers);
        stop = false;
        counter = counter2 = spitCounter = 0;
        limit = Integer.MAX_VALUE;
    }

    @Override
    public void logistics() {
        if (canSpit() && trailers.size() > 0) {
            if (counter2 >= trailers.size())
                counter2 = 0;

            if (++counter % freq == 0) {
                spitCounter++;
                getLevel().add(trailers.get(counter2++).getClone().move(x(), y()));
            }
        }
        super.logistics();
    }

    public void setSpawners(final Entity... trailers) {
        this.trailers = Obj.requireNotEmpty(trailers);
        counter2 = 0;
    }

    public boolean canSpit() {
        return !stop && spitCounter < limit;
    }

    public void spitLimit(final int limit) {
        this.limit = limit;
    }

    public int getSpitCounter() {
        return spitCounter;
    }

    public void stop(final boolean stop) {
        this.stop = stop;
    }
}