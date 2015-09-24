package pojahn.game.entities;

import pojahn.game.core.Entity;

public class Trailer extends PathDrone {

    private Entity[] trailers;
    private int freq, counter, counter2, spitCounter, limit;
    private boolean stop;

    public Trailer(float x, float y) {
        this(x, y, 10, new Entity[0]);
    }

    public Trailer(float x, float y, int freq, Entity... trailers) {
        super(x, y);
        this.freq = freq;
        this.trailers = trailers;
        stop = false;
        counter = counter2 = spitCounter = 0;
        limit = Integer.MAX_VALUE;
    }

    @Override
    public void logics() {
        if (canSpit() && trailers.length > 0) {
            if (counter2 >= trailers.length)
                counter2 = 0;

            if (++counter % freq == 0) {
                spitCounter++;
                getLevel().add(trailers[counter2++].getClone().move(x(), y()));
            }
        }
        super.logics();
    }

    public void setSpawners(Entity... trailers) {
        this.trailers = trailers;
        counter2 = 0;
    }

    public void setFrequency(int freq) {
        this.freq = freq;
        counter = 0;
    }

    public boolean canSpit() {
        return !stop && spitCounter < limit;
    }

    public void spitLimit(int limit) {
        this.limit = limit;
    }

    public int getSpitCounter() {
        return spitCounter;
    }

    public void stop(boolean stop) {
        this.stop = stop;
    }
}