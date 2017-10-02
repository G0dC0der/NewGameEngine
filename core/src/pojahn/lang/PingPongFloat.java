package pojahn.lang;

public class PingPongFloat {

    private boolean increase;
    private float value;
    private float min;
    private float max;
    private float amount;

    public PingPongFloat(final float min, final float max, final float amount) {
        this.min = min;
        this.max = max;
        this.amount = amount;
    }

    public boolean isIncreasing() {
        return increase;
    }

    public void setIncrease(final boolean increase) {
        this.increase = increase;
    }

    public void setMin(final float min) {
        this.min = min;
    }

    public void setMax(final float max) {
        this.max = max;
    }

    public float get() {
        if (increase) {
            value += amount;

            if (value > max) {
                value = max;
                increase = false;
            }
        } else {
            value -= amount;

            if (value < min) {
                value = min;
                increase = true;
            }
        }

        return value;
    }
}
